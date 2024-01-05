package org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.adapter.impl;

import com.alibaba.fastjson.JSONArray;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.jetlinks.szy206.protocol.message.binary.ComprehensiveValue;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.adapter.ControllerAreaFunctionCodeAdapter;
import org.jetlinks.szy206.protocol.message.utils.HexUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;


//0x14
public class Comprehensive implements ControllerAreaFunctionCodeAdapter {

    private static String name = "comprehensive";

    @Override
    public HashMap<String, Object> ControllerAreaRead(ByteBuf buf, JSONArray properties, Boolean collectFlowFlag, Boolean comprehensiveFlag) {
        int data = buf.readByte();//雨量类型数据 （1字节）
        ComprehensiveValue comprehensiveValue = HexUtils.getComprehensiveValue(data);
        ArrayList<ComprehensiveValueType> comprehensiveValueTypes = ComprehensiveValueType.getComprehensiveValueTypes(comprehensiveValue);
        HashMap<String, Object> map = new HashMap<>();
        long count = comprehensiveValueTypes
            .stream()
            .filter(comprehensiveValueType -> comprehensiveValueType != null)
            .count();
        comprehensiveFlag = true;
        for (int i = 0; i < count; i++) {
            ComprehensiveValueType comprehensiveValueType = comprehensiveValueTypes.get(i);
            Supplier<ControllerAreaFunctionCodeAdapter> clazz = comprehensiveValueType.getClazz();
            ControllerAreaFunctionCodeAdapter controllerAreaFunctionCodeAdapter = clazz.get();
            HashMap<String, Object> hashMap = controllerAreaFunctionCodeAdapter.ControllerAreaRead(buf, properties, collectFlowFlag, comprehensiveFlag);
            map.putAll(hashMap);
        }
        return map;
    }


    @Getter
    private enum ComprehensiveValueType {

        waterQuality("水质", WaterQuality::new),
        soilMoistureContent("土壤含水率", SoilMoistureContent::new),
        power("功率", Power::new),
        windSpeed("风速（风向）", WindSpeed::new),
        lockSite("闸位", LockSite::new),
        flowAndWater("流量（水量）", FlowAndWater::new),
        waterLevel("水位", WaterLevel::new),
        rainFall("雨量", RainFall::new);

        private String describe;
        private Supplier<ControllerAreaFunctionCodeAdapter> clazz;

        ComprehensiveValueType(String describe, Supplier<ControllerAreaFunctionCodeAdapter> clazz) {
            this.describe = describe;
            this.clazz = clazz;
        }

        public static ArrayList<ComprehensiveValueType> getComprehensiveValueTypes(ComprehensiveValue comprehensiveValue) {
            ArrayList<Integer> properties = comprehensiveValue.getProperties();
            ArrayList<ComprehensiveValueType> valueTypes = new ArrayList<>();
            ComprehensiveValueType[] values = values();
            for (int i = 0; i < values.length; i++) {
                if (properties.get(i) == 1) {
                    valueTypes.add(values[i]);
                }
            }
            return valueTypes;
        }
    }

//    private ComprehensiveResolveEnum getType(int type) {
//        type = (type >> 6) & 0xF;
//        ComprehensiveResolveEnum resolveEnum = ComprehensiveResolveEnum.fromCode(type);
//        return resolveEnum;
//    }

//    @Getter
//    private enum ComprehensiveResolveEnum {
//        minuteRainfall(0x00, "时段降雨量", "double"),
//        hourRainfall(0x01, "小时降雨量", "double"),
//        dayRainfall(0x10, "日降雨量", "double"),
//        testRainfall(0x11, "测试数据,降雨量为累计雨量", "double");
//        private int code;
//
//        private String describe;
//
//        private String dataType;
//
//
//        ComprehensiveResolveEnum(int code, String describe, String dataType) {
//            this.code = code;
//            this.describe = describe;
//            this.dataType = dataType;
//        }
//
//        protected static ComprehensiveResolveEnum fromCode(int code) {
//            for (ComprehensiveResolveEnum value : values()) {
//                if (value.code == code) {
//                    return value;
//                }
//            }
//            throw new UnsupportedOperationException("不支持的雨量类型" + code);
//        }
//
//        public static JSONObject covertMetadata(ComprehensiveResolveEnum value) {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("id", value.name());
//            jsonObject.put("name", value.getDescribe());
//            JSONObject valueType = new JSONObject();
//            valueType.put("type", value.getDataType());
//            jsonObject.put("valueType", valueType);
//            JSONObject expands = new JSONObject();
//            expands.put("readOnly", "true");
//            expands.put("source", "device");
//            jsonObject.put("expands", expands);
//            return jsonObject;
//        }
//
//    }

}
