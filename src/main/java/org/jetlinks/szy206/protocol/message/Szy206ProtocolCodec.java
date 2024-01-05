package org.jetlinks.szy206.protocol.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.jetlinks.core.cluster.ClusterCache;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DeviceOfflineMessage;
import org.jetlinks.core.message.DeviceRegisterMessage;
import org.jetlinks.core.message.DisconnectDeviceMessage;
import org.jetlinks.core.message.codec.*;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.types.BooleanType;
import org.jetlinks.core.server.session.DeviceSession;
import org.jetlinks.pro.ConfigMetadataConstants;
import org.jetlinks.supports.cluster.redis.RedisClusterManager;
import org.jetlinks.szy206.protocol.message.additional.Szy206Header;
import org.jetlinks.szy206.protocol.message.binary.BinaryMessageType;
import org.jetlinks.szy206.protocol.message.binary.SzyResult;
import org.jetlinks.szy206.protocol.message.handler.MetadataHandler;
import org.jetlinks.szy206.protocol.message.handler.PackageHandler;
import org.jetlinks.szy206.protocol.message.utils.ControllerAreaUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class Szy206ProtocolCodec implements DeviceMessageCodec {

    public static final String collectFlowFlag = "collectFlowFlag";

    private final String productId = "1737364854070452224";


    public static final DefaultConfigMetadata szy206Metadata =
        new DefaultConfigMetadata("需要采集瞬时流量", "是否需要采集瞬时流量")
            .add(collectFlowFlag, "collectFlowFlag", "瞬时流量采集标识",
                 new BooleanType());

    private DeviceRegistry registry;

    private ClusterCache<String, Object> redis;

    @Override
    public Transport getSupportTransport() {
        return DefaultTransport.TCP;
    }

    @Override
    public Mono<? extends MessageCodecDescription> getDescription() {
        return DeviceMessageCodec.super.getDescription();
    }

    public Szy206ProtocolCodec(DeviceRegistry registry, RedisClusterManager redisClusterManager) {
        this.registry = registry;
        this.redis = redisClusterManager.createCache("szy206");
    }


    public Szy206ProtocolCodec(DeviceRegistry registry) {
        this.registry = registry;
    }


    public static ByteBuf wrapByteByf(ByteBuf payload) {

        return Unpooled.wrappedBuffer(payload);
    }

    @Nonnull
    @Override
    public Flux<DeviceMessage> decode(@Nonnull MessageDecodeContext context) {
        ByteBuf payload = context.getMessage().getPayload();

        log.warn("收到设备报文===>{}", Hex.encodeHexString(payload.array()).toUpperCase());
        return getDeviceId(payload).flatMap(deviceId -> {
            return handlerDeviceMessage(context, payload, deviceId);
        });
    }

    public Flux<String> getDeviceId(ByteBuf payload) {
        ByteBuf duplicate = payload.duplicate();
        duplicate.readByte();
        duplicate.readByte();
        duplicate.readByte();
        int c = duplicate.readByte();
        int div = ControllerAreaUtils.getDiv(c);
        if (div == 1) {
            duplicate.readByte();
        }
        ByteBuf byteBuf = duplicate.readBytes(5);
        return Flux.just(Hex.encodeHexString(byteBuf.array()).toUpperCase());
    }

    private Flux<DeviceMessage> handlerDeviceMessage(MessageDecodeContext context, ByteBuf payload, String deviceId) {
        ByteBuf duplicate = payload.duplicate();
        return registry.getDevice(deviceId).flatMapMany(device -> {
            return device.getConfig(collectFlowFlag).flatMapMany(value -> {
                Boolean collectFlowFlag = value.as(Boolean.class);//是否采集流量数据
                return Flux.just(collectFlowFlag);
            }).switchIfEmpty(Flux.just(false)).flatMap(flag -> {
                return Flux
                    .just(BinaryMessageType.read(payload, flag))
                    .filter(szyResult -> szyResult.getMessages().size() > 0)
                    .flatMap(szyResult -> {
                        if (context instanceof FromDeviceMessageContext) {
                            DeviceSession session = ((FromDeviceMessageContext) context).getSession();
                            return populationIPAndPort(szyResult, context).flatMap(szyResultNew -> {
                                if (!Objects.isNull(szyResultNew.getSzy206ImageData())) { //存在分包
                                    return new PackageHandler().handler(redis, szyResultNew, device, session);
                                }
                                return session
                                    .send(EncodedMessage.simple(szyResultNew.getAsk()))
                                    .flatMapMany(ignore -> {
                                        return MetadataHandler
                                            .toDeviceMessage(device, szyResultNew.getMessages(), szyResultNew.getMetadata())
                                            .flatMap(deviceMessage -> {
                                                if (deviceMessage instanceof DeviceOfflineMessage) {
                                                    session.close();
                                                }
                                                return Flux.just(deviceMessage);
                                            });
                                    });
                            });
                        }
                        return Flux.empty();
                    });
            });
        }).switchIfEmpty(Flux.defer(() -> {
            log.warn("设备不存在，尝试自注册中");
            //考虑设备自注册？
            DeviceRegisterMessage registerMessage = new DeviceRegisterMessage();
            Map<String, Object> headers = new HashMap<>();
            headers.put("productId", productId); //需要注册的产品id
            headers.put("deviceName", productId + "_" + deviceId); //需要注册的设备名称
            registerMessage.setDeviceId(deviceId);
            registerMessage.setTimestamp(System.currentTimeMillis());
            registerMessage.setHeaders(headers);
            return Flux.concat(Flux.just(registerMessage), Mono
                .delay(Duration.ofSeconds(2))
                .thenMany(handlerDeviceMessage(context, duplicate, deviceId)));
        }));
    }

    @Nonnull
    @Override
    public Publisher<? extends EncodedMessage> encode(@Nonnull MessageEncodeContext context) {
        DeviceMessage deviceMessage = ((DeviceMessage) context.getMessage());
        if (deviceMessage instanceof DisconnectDeviceMessage) {
            return Mono.empty();
        }
        return Mono.just(EncodedMessage.simple(wrapByteByf(BinaryMessageType.write(deviceMessage, Unpooled.buffer()))));
    }

    private Flux<SzyResult> populationIPAndPort(SzyResult szyResult, MessageDecodeContext context) {
        if (Objects.isNull(szyResult.getSzy206Header())) {
            Szy206Header szy206Header = Szy206Header.getSzy206Header();
            szyResult.setSzy206Header(szy206Header);
        }
        Szy206Header szy206Header = szyResult.getSzy206Header();
        DeviceSession session = ((FromDeviceMessageContext) context).getSession();
        session.getClientAddress().ifPresent(inetSocketAddress -> {
            String iP = inetSocketAddress.getHostString();
            int port = inetSocketAddress.getPort();
            szy206Header.populationPORT(port);
            szy206Header.populationIP(iP);
        });
        return Flux.just(szyResult);
    }
}
