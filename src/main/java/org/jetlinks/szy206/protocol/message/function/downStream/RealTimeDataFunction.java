package org.jetlinks.szy206.protocol.message.function.downStream;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bouncycastle.util.encoders.Hex;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.function.FunctionParameter;
import org.jetlinks.szy206.protocol.message.binary.BinaryMessage;
import org.jetlinks.szy206.protocol.message.binary.BinaryMessageType;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.ControllerAreaFunctionCode;
import org.jetlinks.szy206.protocol.message.utils.HexUtils;
import org.jetlinks.szy206.protocol.message.utils.Szy206MessageUtils;

import java.util.List;

public class RealTimeDataFunction implements BinaryMessage<FunctionInvokeMessage> {

    private static String TIMES = "times";
    private static String VALUECODE = "valueCode";

    @Override
    public BinaryMessageType getType() {
        return BinaryMessageType.RealTimeData;
    }

    @Override
    public void read(ByteBuf payload, int length, byte[] addressArea, int areaFunctionCode, Boolean collectFlowFlag) {

    }

    @Override
    public void write(ByteBuf buf, DeviceMessage message) {
        if (message instanceof FunctionInvokeMessage) {
            FunctionInvokeMessage functionInvokeMessage = (FunctionInvokeMessage) message;
            int times = Szy206MessageUtils.getIntParameter(functionInvokeMessage, TIMES);
            int valueCode = Szy206MessageUtils.getIntParameter(functionInvokeMessage, VALUECODE);
            int c = Szy206MessageUtils.getC(times, valueCode);
            String deviceId = message.getDeviceId();
            ByteBuf payload = Unpooled.buffer();
            payload.writeByte(c);//C
            payload.writeBytes(Hex.decode(deviceId));//A
            payload.writeByte(getType().getFunctionCode());//AFN
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