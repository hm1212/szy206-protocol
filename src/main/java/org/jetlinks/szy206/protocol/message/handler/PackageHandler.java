package org.jetlinks.szy206.protocol.message.handler;

import io.netty.buffer.Unpooled;
import org.apache.commons.codec.binary.Hex;
import org.jetlinks.core.cluster.ClusterCache;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.codec.EncodedMessage;
import org.jetlinks.core.message.codec.FromDeviceMessageContext;
import org.jetlinks.core.message.codec.MessageDecodeContext;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.core.server.session.DeviceSession;
import org.jetlinks.szy206.protocol.message.additional.Szy206Header;
import org.jetlinks.szy206.protocol.message.binary.SzyResult;
import org.jetlinks.szy206.protocol.message.binary.images.Szy206ImageData;
import org.jetlinks.szy206.protocol.message.utils.Szy206MessageUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

public class PackageHandler {

    private static String Image = "image";//图片数据

    public  Flux<DeviceMessage> handler(ClusterCache<String, Object> redis, SzyResult szyResult,
                                       DeviceOperator device, DeviceSession session) {

        Szy206ImageData szy206ImageData = szyResult.getSzy206ImageData();
        int div = szy206ImageData.getDivs();
        String imageListKey = Md5Utils.Md5Hex(device.getDeviceId(), 1);
        if (div != 1) {
            return storeImageData(redis, szyResult, device, session, imageListKey, div, szy206ImageData);
        }
        //dev==1，表示最后一帧数据
        return redis.get(imageListKey).flatMapMany(list -> {
            ArrayList<String> arrayList = (ArrayList<String>) list;
            arrayList.set(div, szy206ImageData.toJsonString());
            return redis.put(imageListKey, arrayList).flatMapMany(ignore0 -> {
                return completeFlag(arrayList).flatMap(completeFlag -> {
                    return ask(szyResult, session).thenMany(getEventMessage(szyResult, device, szy206ImageData,
                                                                            completeFlag));
                });
            });
        });
    }


    /**
     * 存储图片数据到redis
     *
     * @param redis
     * @param szyResult
     * @param device
     * @param session
     * @param imageListKey
     * @param div
     * @param szy206ImageData
     * @return
     */
    private Flux<DeviceMessage> storeImageData(ClusterCache<String, Object> redis,
                                               SzyResult szyResult,
                                               DeviceOperator device,
                                               DeviceSession session,
                                               String imageListKey,
                                               int div,
                                               Szy206ImageData szy206ImageData) {
        return redis.get(imageListKey).flatMapMany(list -> {
            ArrayList<String> arrayList = (ArrayList<String>) list;
            arrayList.set(div, szy206ImageData.toJsonString());
            return redis.put(imageListKey, arrayList).flatMapMany(ignore0 -> {
                //可能为最后一包数据，即div=1先处理完
                return completeFlag(arrayList).flatMap(completeFlag -> {
                    if (completeFlag) {
                        byte[] comBain = MultiPackageHandler.comBain(arrayList);
                        szy206ImageData.setSubData(comBain);
                        return ask(szyResult, session).flatMapMany(ignore -> {
                            return getEventMessage(szyResult, device, szy206ImageData, completeFlag);
                        });
                    }
                    return ask(szyResult, session).flatMapMany(ignore -> {
                        return getEventMessage(szyResult, device, szy206ImageData, completeFlag);
                    });
                });

            });
        }).switchIfEmpty(Flux.defer(() -> {
            int allDivs = szy206ImageData.getDivs();
            ArrayList<String> arrayList = new ArrayList<>(allDivs);
            for (int i = 1; i <= allDivs + 1; i++) {
                arrayList.add(null);
            }
            String imageStr = szyResult.getSzy206ImageData().toJsonString();
            arrayList.set(allDivs, imageStr);
            return redis.put(imageListKey, arrayList).flatMapMany(ignore -> {
                return ask(szyResult, session).thenMany(
                    getEventMessage(szyResult, device, szy206ImageData, false));
            });
        }));
    }

    /**
     * 判断分包是否完成
     *
     * @param arrayList
     * @return
     */
    private Flux<Boolean> completeFlag(ArrayList<String> arrayList) {
        int count = (int) arrayList.stream().filter(str -> str != null).count();
        int size = arrayList.size();
        if (count == size) {//所有包数据已完成，开始组包
            return Flux.just(true);
        }
        return Flux.just(false);
    }


    /**
     * 回复设备消息
     *
     * @param szyResult
     * @return
     */
    private Mono<Boolean> ask(SzyResult szyResult, DeviceSession session) {
        return session
            .send(EncodedMessage.simple(szyResult.getAsk()));
    }


    /**
     * 封装事件上报消息
     *
     * @param szyResult
     * @param device
     * @param szy206ImageData
     * @param completeFlag
     * @return
     */
    private Flux<DeviceMessage> getEventMessage(SzyResult szyResult,
                                                DeviceOperator device,
                                                Szy206ImageData szy206ImageData,
                                                boolean completeFlag) {
        EventMessage eventMessage = new EventMessage();
        eventMessage.setDeviceId(device.getDeviceId());
        Map<String, Object> properties = new HashMap<>();
        eventMessage.setData(properties);
        if (!completeFlag) {
            eventMessage.setEvent("subDiv");
            properties.put("currentDiv", szy206ImageData.getDivs());
        } else {
            eventMessage.setEvent(Image);
        }
        properties.put("data", Hex.encodeHexString(szy206ImageData.getSubData()).toUpperCase());
        Szy206MessageUtils.addSL651MessageHeader(szyResult.getSzy206Header(), eventMessage);
        return Flux.just(eventMessage);
    }

}
