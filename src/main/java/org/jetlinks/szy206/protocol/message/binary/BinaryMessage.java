package org.jetlinks.szy206.protocol.message.binary;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import org.jetlinks.core.message.DeviceMessage;

public interface BinaryMessage<T extends DeviceMessage> {
    BinaryMessageType getType();

    void read(ByteBuf payload, int length, byte[] addressArea, int areaFunctionCode, Boolean collectFlowFlag);

    void write(ByteBuf buf, DeviceMessage message);

    void setMessage(T message);

    T getMessage();

    ByteBuf getAskPayload();

    JSONObject getMetadata();
}
