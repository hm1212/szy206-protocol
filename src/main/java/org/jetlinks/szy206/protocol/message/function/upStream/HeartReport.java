package org.jetlinks.szy206.protocol.message.function.upStream;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.apache.commons.codec.binary.Hex;
import org.jetlinks.core.message.DeviceLogMessage;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.szy206.protocol.message.binary.BinaryMessage;
import org.jetlinks.szy206.protocol.message.binary.BinaryMessageType;
import org.jetlinks.szy206.protocol.message.utils.ControllerAreaUtils;
import org.jetlinks.szy206.protocol.message.utils.HexUtils;

import java.util.HashMap;

public class HeartReport implements BinaryMessage<EventMessage> {

    public static String HEARTDATA = "heartData";
    public static String HEART = "heart";

    private EventMessage message;

    private JSONObject metadata;
    private ByteBuf askPayload;

    @Override
    public BinaryMessageType getType() {
        return BinaryMessageType.Heart;
    }

    @Override
    public void read(ByteBuf payload, int length, byte[] addressArea, int areaFunctionCode, Boolean collectFlowFlag) {
        message = new EventMessage();
        String deviceId = Hex.encodeHexString(addressArea);
        JSONArray properties = new JSONArray();
        setMetadata(properties);
        int heartDataCode = payload.readUnsignedByte();
        HeartEnum heartData = HeartEnum.getHeartData(heartDataCode);
        String data = heartData.getDescribe();
        HashMap<String, Object> map = new HashMap<>();
        map.put(HEARTDATA, data);
        message.setData(map);
        message.setEvent(HEART);
        message.setDeviceId(deviceId);
        message.setTimestamp(System.currentTimeMillis());
        setAskPayload(payload);
    }

    @Override
    public void write(ByteBuf buf, DeviceMessage message) {

    }

    @Override
    public void setMessage(EventMessage message) {
        this.message = message;
    }


    @Override
    public EventMessage getMessage() {
        return message;
    }

    @Override
    public ByteBuf getAskPayload() {
        return askPayload;
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
        askPayload.writeByte(0x02);//AFN
        payload.readByte(); //跳过一字节
        askPayload.writeByte(0x00);//数据域（1 个字节）
        System.out.println(Hex.encodeHex(askPayload.array()));
        int crc8 = HexUtils.getCrc8(askPayload);
        askPayload.writeByte(crc8);
    }

    private void setMetadata(JSONArray properties) {
        metadata = new JSONObject();
        metadata.put("events", new JSONArray());
        metadata.put("functions", new JSONArray());
        metadata.put("tags", new JSONArray());
        metadata.put("properties", properties);
    }

    @Override
    public JSONObject getMetadata() {
        return metadata;
    }

    @Getter
    public enum HeartEnum {
        F0(0xF0, "登录"),
        F1(0xF1, "退出登录"),
        F2(0xF2, "在线保持");
        private int code;
        private String describe;

        HeartEnum(int code, String describe) {
            this.code = code;
            this.describe = describe;
        }

        public static HeartEnum getHeartData(int code) {
            for (HeartEnum value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
            throw new UnsupportedOperationException("不支持的心跳类型" + Integer.toHexString(code));
        }
    }

}
