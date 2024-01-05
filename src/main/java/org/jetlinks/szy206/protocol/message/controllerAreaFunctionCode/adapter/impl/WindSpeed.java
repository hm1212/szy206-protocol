package org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.adapter.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.ControllerAreaFunctionCode;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.adapter.ControllerAreaFunctionCodeAdapter;
import org.jetlinks.szy206.protocol.message.utils.HexUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

//0x08
public class WindSpeed implements ControllerAreaFunctionCodeAdapter {

    private static String name = "windSpeed";

    @Override
    public HashMap<String, Object> ControllerAreaRead(ByteBuf buf, JSONArray properties, Boolean collectFlowFlag, Boolean comprehensiveFlag) {
        ByteBuf byteBuf = buf.readBytes(3);
        int num = byteBuf.getByte(2);
        BigDecimal value = HexUtils.byteBuf2BigDecimal(byteBuf, 2, true, true);
        ControllerAreaFunctionCode code = ControllerAreaFunctionCode.fromName(name);
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<Object> list = new ArrayList<>();
        list.add(value);
        //计算风向
        int windDirection = (num >> 4) & 0xF;
        list.add(windDirection);
        JSONObject jsonObject = ControllerAreaFunctionCode.covertMetadata(name, "array");
        properties.add(jsonObject);
        map.put(code.name(), list);
        return map;
    }
}
