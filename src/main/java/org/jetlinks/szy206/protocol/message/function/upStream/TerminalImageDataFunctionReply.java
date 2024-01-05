package org.jetlinks.szy206.protocol.message.function.upStream;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.codec.binary.Hex;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.szy206.protocol.message.binary.BinaryMessage;
import org.jetlinks.szy206.protocol.message.binary.BinaryMessageType;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.ControllerAreaFunctionCode;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.adapter.ControllerAreaFunctionCodeAdapter;
import org.jetlinks.szy206.protocol.message.state.TerminalAlarmState;
import org.jetlinks.szy206.protocol.message.state.TerminalSate;
import org.jetlinks.szy206.protocol.message.utils.ControllerAreaUtils;
import org.jetlinks.szy206.protocol.message.utils.HexUtils;

import java.util.HashMap;

public class TerminalImageDataFunctionReply implements BinaryMessage<ReportPropertyMessage> {

    public static String imageHexKey = "imageHex";

    private ReportPropertyMessage message;
    private ByteBuf askPayload;
    private JSONObject metadata;

    @Override
    public BinaryMessageType getType() {
        return BinaryMessageType.TerminalImageDataReply;
    }

    @Override
    public void read(ByteBuf payload, int length, byte[] addressArea, int areaFunctionCode, Boolean collectFlowFlag) {
        message = new ReportPropertyMessage();
        JSONArray properties = new JSONArray();
        setMetadata(properties);
        String deviceId = Hex.encodeHexString(addressArea);
        message.setDeviceId(deviceId);
        payload.readBytes(2);//图片报文长度
        ByteBuf imageBuf = payload.readBytes(length - 4);
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put(imageHexKey, Hex.encodeHexString(imageBuf.array()));
        message.setProperties(resultMap);
        setAskPayload(payload);
    }

    private void setAskPayload(ByteBuf payload) {
        //构建回复报文
        payload.resetReaderIndex();//反转报文
        // payload = payload.duplicate();
        askPayload = Unpooled.buffer();
        payload.readBytes(3);//68H L 68H
        int ControllerArea = payload.readByte();//C
        askPayload.writeByte(ControllerArea & 0x7F);
        int div = ControllerAreaUtils.getDiv(ControllerArea);
        if (div > 0) {
            askPayload.writeByte(payload.readByte());
        }
        byte[] addressBytes = new byte[5];
        payload.readBytes(addressBytes);//A
        askPayload.writeBytes(addressBytes);
        askPayload.writeByte(0xC0);//AFN
        payload.readByte(); //跳过一字节
        askPayload.writeByte(0x00);//数据域（1 个字节）
        System.out.println(Hex.encodeHex(askPayload.array()));
        int crc8 = HexUtils.getCrc8(askPayload);
        askPayload.writeByte(crc8);
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
        return askPayload;
    }

    @Override
    public JSONObject getMetadata() {
        return metadata;
    }

    private void setMetadata(JSONArray properties) {
        metadata = new JSONObject();
        metadata.put("events", new JSONArray());
        metadata.put("functions", new JSONArray());
        metadata.put("tags", new JSONArray());
        metadata.put("properties", properties);
    }
}
