package org.jetlinks.szy206.protocol.message.state;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetlinks.core.utils.BytesUtils;
import org.jetlinks.szy206.protocol.message.binary.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 数据域中前 2 个字节给出报警种类和报警状态，各位表示为：0 不报警，1 报警
 */
@Getter
public enum TerminalAlarmState {

    WorkACPowerOutageAlarm(0, "工作交流电停电告警", "不报警", "报警"),
    BatteryVoltageAlarm(0, "蓄电池电压报警", "不报警", "报警"),
    WaterLevelExceedingLimitAlarm(0, "水位超限报警", "不报警", "报警"),
    FlowOverLimitAlarm(0, "流量超限报警", "不报警", "报警"),
    WaterQualityExceedingLimitAlarm(0, "水质超限报警", "不报警", "报警"),
    FlowMeterMalfunctionAlarm(0, "流量仪表故障报警", "不报警", "报警"),
    WaterPumpState(0, "水泵开停状态", "不报警", "报警"),
    WaterLevelInstrumentMalfunctionAlarm(0, "水位仪表故障报警", "不报警", "报警"),
    WaterPressureExceedingLimitAlarm(0, "水压超限报警", "不报警", "报警"),
    back9(0, "备用9", "", ""),
    TerminalICcardFunctionAlarm(0, "终端IC卡功能报警", "不报警", "报警"),
    FixedValueControlAlarm(0, "定值控制报警", "不报警", "报警"),
    RemainingWaterAlarm(0, "剩余水量的下限报警", "不报警", "报警"),
    TerminalBoxDoorStatusAlarm(0, "终端箱门状态报警", "不报警", "报警"),
    back14(0, "备用14", "", ""),
    back15(0, "备用15", "", "");

    @Setter
    private int code;
    private final static String dataType = "enum";
    @Getter
    private String describe;
    private String element0;
    private String element1;


    TerminalAlarmState(int code, String describe, String element0, String element1) {
        this.code = code;
        this.describe = describe;
        this.element0 = element0;
        this.element1 = element1;
    }

    public static HashMap<String, Object> resolveBit(ByteBuf bytebuf, String name, JSONArray properties) {
        byte[] targetBytes = new byte[bytebuf.readableBytes()];
        //ArrayList<HashMap<String, Integer>> list = new ArrayList<>();
        HashMap<String, Object> resultMap = new HashMap<>();
        bytebuf.readBytes(targetBytes, 0, targetBytes.length);
        int num = BytesUtils.beToInt(targetBytes);
        Arrays.sort(TerminalAlarmState.values());
        for (TerminalAlarmState value : TerminalAlarmState.values()) {
            int bit = num & 0x1;
            if (!(value.name().startsWith("back"))) {
                JSONObject jsonObject = TerminalAlarmState.covertMetadata(value);
                properties.add(jsonObject);
                resultMap.put(value.name(), bit);
            }
            num = num >> 1;
        }
        return resultMap;
    }

    public static JSONObject covertMetadata(TerminalAlarmState value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", value.name());
        jsonObject.put("name", value.describe);
        JSONObject valueType = new JSONObject();
        ArrayList<Value> elements = new ArrayList<>();
        elements.add(new Value("0", value.getElement0()));
        elements.add(new Value("1", value.getElement1()));
        valueType.put("elements", elements);
        valueType.put("type", TerminalAlarmState.dataType);
        jsonObject.put("valueType", valueType);
        JSONObject expands = new JSONObject();
        expands.put("readOnly", "true");
        expands.put("source", "device");
        jsonObject.put("expands", expands);
        return jsonObject;
    }
}
