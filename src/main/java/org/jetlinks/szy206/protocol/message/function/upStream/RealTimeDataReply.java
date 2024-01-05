package org.jetlinks.szy206.protocol.message.function.upStream;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.szy206.protocol.message.binary.BinaryMessage;
import org.jetlinks.szy206.protocol.message.binary.BinaryMessageType;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.ControllerAreaFunctionCode;

/**
 * 查询遥测终端实时值 回复数据
 */
public class RealTimeDataReply implements BinaryMessage<ReportPropertyMessage> {
    private ReportPropertyMessage message;

    @Override
    public BinaryMessageType getType() {
        return BinaryMessageType.RealTimeDataReply;
    }

    @Override
    public void read(ByteBuf payload, int length, byte[] addressArea, int areaFunctionCode, Boolean collectFlowFlag) {
        message = new ReportPropertyMessage();
        ByteBuf userData = payload.readBytes(length);//数据域
        ControllerAreaFunctionCode cafc = ControllerAreaFunctionCode.fromCode(areaFunctionCode);//控制域功能定义了被查询实时参数种类和数量

    }

    @Override
    public void write(ByteBuf buf, DeviceMessage message) {

    }

    @Override
    public void setMessage(ReportPropertyMessage message) {

    }

    @Override
    public ReportPropertyMessage getMessage() {
        return message;
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
