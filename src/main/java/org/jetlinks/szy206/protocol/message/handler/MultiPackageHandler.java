package org.jetlinks.szy206.protocol.message.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.jetlinks.szy206.protocol.message.binary.images.Szy206ImageData;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
public class MultiPackageHandler {
    public static byte[] comBain(ArrayList<String> imageDataList) {

        ByteBuf result = Unpooled.buffer();
        imageDataList
            .stream()
            .filter(s -> !Objects.isNull(s))
            .map(str -> {
                Szy206ImageData szy206ImageData = Szy206ImageData.fromJsonString(str);
                return szy206ImageData.getSubData();
            })
            .forEach(subData -> result.writeBytes(subData));
        byte[] payload = new byte[result.readableBytes()];
        System.arraycopy(result.array(), 0, payload, 0, payload.length);
        log.warn("本次总数据===>{}", Hex.encodeHexString(payload));
        return payload;
    }

}