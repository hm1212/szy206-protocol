package org.jetlinks.szy206.protocol.message.state;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.jetlinks.core.utils.BytesUtils;
import org.jetlinks.szy206.protocol.message.binary.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@Getter
public enum TerminalSate {
    TerminalWorkMode(0, "终端的工作模式", "终端在自报、遥测工作状态", "终端在自报确认工作状态", "终端在遥测工作状态", "终端在调试或维修状态"),
    ICCardEffective(0, "终端 IC 卡功能是否有效", "开启", "关闭", "", ""),
    FixedValueControl(0, "定值控制是否投入", "开启", "关闭", "", ""),
    waterPumpWokeState(0, "水泵工作状态", "开启", "关闭", "", ""),
    TerminalBoxDoorState(0, "终端箱门状态", "开启", "关闭", "", ""),
    PowerWokeState(0, "电源工作状态", "AC220V 供电", "蓄电池供电", "", ""),
    back7(0, "备用7", "", "", "", ""),
    back8(0, "备用8", "", "", "", ""),
    back9(0, "备用9", "", "", "", ""),
    back10(0, "备用10", "", "", "", ""),
    back11(0, "备用11", "", "", "", ""),
    back12(0, "备用12", "", "", "", ""),
    back13(0, "备用13", "", "", "", ""),
    back14(0, "备用14", "", "", "", ""),
    back15(0, "备用15", "", "", "", "");

    private int code;

    private final static  String dataType="enum";
    private String describe;
    private String element0;
    private String element1;
    private String element2;
    private String element3;


    TerminalSate(int code, String describe, String element0, String element1, String element2, String element3) {
        this.code = code;
        this.describe = describe;
        this.element0 = element0;
        this.element1 = element1;
        this.element2 = element2;
        this.element3 = element3;
    }

    public static HashMap<String, Object> resolveBit(ByteBuf bytebuf,
                                                     String name, JSONArray properties) {
        byte[] targetBytes = new byte[bytebuf.readableBytes()];
        HashMap<String, Object> resultMap = new HashMap<>();
        bytebuf.readBytes(targetBytes, 0, targetBytes.length);
        int num = BytesUtils.beToInt(targetBytes);
        Arrays.sort(TerminalSate.values());
        for (TerminalSate value : TerminalSate.values()) {
            int bit = num & 0x1;
            if (!(value.name().startsWith("back"))) {
                JSONObject jsonObject = TerminalSate.covertMetadata(value);
                properties.add(jsonObject);
                resultMap.put(value.name(), bit);
            }
            num = num >> 1;
        }
        return resultMap;
    }

    public static JSONObject covertMetadata(TerminalSate value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", value.name());
        jsonObject.put("name", value.describe);
        JSONObject valueType = new JSONObject();
        ArrayList<Value> elements = new ArrayList<>();
        elements.add(new Value("0", value.getElement0()));
        elements.add(new Value("1", value.getElement1()));
        if (value.ordinal() == 1) {
            elements.add(new Value("2", value.getElement2()));
            elements.add(new Value("3", value.getElement3()));
        }
        valueType.put("elements", elements);
        valueType.put("type", TerminalSate.dataType);
        jsonObject.put("valueType", valueType);
        JSONObject expands = new JSONObject();
        expands.put("readOnly", "true");
        expands.put("source", "device");
        jsonObject.put("expands", expands);
        return jsonObject;
    }

}
