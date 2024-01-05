package org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.adapter.ControllerAreaFunctionCodeAdapter;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.adapter.impl.*;

import java.util.Objects;
import java.util.function.Supplier;

@Getter
public enum ControllerAreaFunctionCode {


    ack(0x00, "发送/确认", "命令", "text", null),
    rainFall(0x01, "查询∕响应帧", "雨量", "text", RainFall::new),
    waterLevel(0x02, "查询∕响应帧", "水位", "text", WaterLevel::new),
    flowAndWater(0x03, "查询∕响应帧", "流量（水量）", "array", FlowAndWater::new),
    speed(0x04, "查询∕响应帧", "流速", "text", Speed::new),
    lockSite(0x05, "查询∕响应帧", "闸位", "text", LockSite::new),
    power(0x06, "查询∕响应帧", "功率", "text", Power::new),
    airPressure(0x07, "查询∕响应帧", "气压", "text", AirPressure::new),
    windSpeed(0x08, "查询∕响应帧", "风速", "text", WindSpeed::new),
    waterTemperature(0x09, "查询∕响应帧", "水温", "text", WaterTemperature::new),
    waterQuality(10, "查询∕响应帧", "水质", "text", WaterQuality::new),
    soilMoistureContent(11, "查询∕响应帧", "土壤含水率", "text", SoilMoistureContent::new),
    evaporationCapacity(12, "查询∕响应帧", "蒸发量", "text", EvaporationCapacity::new),
    alarmOrStatus(13, "查询∕响应帧", "报警或状态", "text", null),
    comprehensive(14, "查询∕响应帧", "综合参数/统计雨量", "text", Comprehensive::new),
    waterPressure(15, "查询∕响应帧", "水压", "text", WaterPressure::new);

    private int code;
    private String type;
    private String category;

    private String dataType;

    private Supplier<ControllerAreaFunctionCodeAdapter> clazz;

    ControllerAreaFunctionCode(int code, String type, String category, String dataType, Supplier<ControllerAreaFunctionCodeAdapter> clazz) {
        this.code = code;
        this.type = type;
        this.category = category;
        this.dataType = dataType;
        this.clazz = clazz;
    }

    public static ControllerAreaFunctionCode fromCode(int code) {
        for (ControllerAreaFunctionCode value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new UnsupportedOperationException("不支持的功能:" + code);
    }

    public static ControllerAreaFunctionCode fromName(String name) {
        for (ControllerAreaFunctionCode value : values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        throw new UnsupportedOperationException("不支持的枚举对象:" + name);
    }


    public static JSONObject covertMetadata(String name,String type) {
        ControllerAreaFunctionCode value = fromName(name);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", value.name());
        jsonObject.put("name", value.getCategory());
        JSONObject valueType = new JSONObject();
        if (Objects.isNull(type)) {
            valueType.put("type", value.getDataType());
        }else {
            JSONObject elementType = new JSONObject();
            elementType.put("type", "text");
            valueType.put("elementType", elementType);
            valueType.put("type", type);
        }
        jsonObject.put("valueType", valueType);
        JSONObject expands = new JSONObject();
        expands.put("readOnly", "true");
        expands.put("source", "device");
        jsonObject.put("expands", expands);
        return jsonObject;
    }
}
