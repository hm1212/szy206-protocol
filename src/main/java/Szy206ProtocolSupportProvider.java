import org.jetlinks.core.ProtocolSupport;
import org.jetlinks.core.defaults.CompositeProtocolSupport;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.message.codec.DefaultTransport;
import org.jetlinks.core.spi.ProtocolSupportProvider;
import org.jetlinks.core.spi.ServiceContext;
import org.jetlinks.supports.cluster.redis.RedisClusterManager;
import org.jetlinks.supports.official.JetLinksDeviceMetadataCodec;
import org.jetlinks.szy206.protocol.message.Szy206ProtocolCodec;
import reactor.core.publisher.Mono;

public class Szy206ProtocolSupportProvider implements ProtocolSupportProvider {
    @Override
    public Mono<? extends ProtocolSupport> create(ServiceContext context) {
        CompositeProtocolSupport support = new CompositeProtocolSupport();
        support.setName("SZY206水资源监测");
        support.setId("SZY206");
        support.setDescription("国标SZY206水资源监测设备协议");
        support.setMetadataCodec(new JetLinksDeviceMetadataCodec());
        RedisClusterManager redisClusterManager = context
            .getService(RedisClusterManager.class)
            .orElseThrow(IllegalStateException::new);
        context.getService(DeviceRegistry.class)
               .ifPresent(registry -> {
                   Szy206ProtocolCodec codec = new Szy206ProtocolCodec(registry, redisClusterManager);
                   support.addMessageCodecSupport(codec);
               });
        support.addConfigMetadata(DefaultTransport.TCP, Szy206ProtocolCodec.szy206Metadata);
        return Mono.just(support);
    }
}
