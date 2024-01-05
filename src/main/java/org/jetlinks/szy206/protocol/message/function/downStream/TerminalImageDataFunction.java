package org.jetlinks.szy206.protocol.message.function.downStream;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bouncycastle.util.encoders.Hex;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.szy206.protocol.message.binary.BinaryMessage;
import org.jetlinks.szy206.protocol.message.binary.BinaryMessageType;
import org.jetlinks.szy206.protocol.message.utils.HexUtils;
import org.jetlinks.szy206.protocol.message.utils.Szy206MessageUtils;

public class TerminalImageDataFunction implements BinaryMessage<FunctionInvokeMessage> {

    private static String VALUECODE = "valueCode";
    private static String TIMES = "times";

    @Override
    public BinaryMessageType getType() {
        return BinaryMessageType.TerminalImageData;
    }

    @Override
    public void read(ByteBuf payload, int length, byte[] addressArea, int areaFunctionCode, Boolean collectFlowFlag) {

    }

    @Override
    public void write(ByteBuf buf, DeviceMessage message) {
        if (message instanceof FunctionInvokeMessage) {
            FunctionInvokeMessage functionInvokeMessage = (FunctionInvokeMessage) message;
            String deviceId = message.getDeviceId();
            int times = Szy206MessageUtils.getIntParameter(functionInvokeMessage, TIMES);
            int c = Szy206MessageUtils.getC(times, 0);
            ByteBuf payload = Unpooled.buffer();
            payload.writeByte(c);
            payload.writeBytes(Hex.decode(deviceId));
            payload.writeByte(getType().getFunctionCode());
            payload.writeByte(0x01);
            int crc8 = HexUtils.getCrc8(payload);
            payload.writeByte(crc8);
            buf.writeByte(0x68);
            buf.writeByte(payload.readableBytes() - 1);
            buf.writeByte(0x68);
            buf.writeBytes(payload, payload.readerIndex(), payload.readableBytes());
            buf.writeByte(0x16);
        }
    }

    @Override
    public void setMessage(FunctionInvokeMessage message) {

    }

    @Override
    public FunctionInvokeMessage getMessage() {
        return null;
    }

    @Override
    public ByteBuf getAskPayload() {
        return null;
    }

    @Override
    public JSONObject getMetadata() {
        return null;
    }
}
