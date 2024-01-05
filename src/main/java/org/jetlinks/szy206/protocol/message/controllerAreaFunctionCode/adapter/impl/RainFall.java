package org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.adapter.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.ControllerAreaFunctionCode;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.adapter.ControllerAreaFunctionCodeAdapter;
import org.jetlinks.szy206.protocol.message.utils.HexUtils;

import java.math.BigDecimal;
import java.util.HashMap;

//0x01
public class RainFall implements ControllerAreaFunctionCodeAdapter {

    private static String name = "rainFall";

    @Override
    public HashMap<String, Object> ControllerAreaRead(ByteBuf buf, JSONArray properties, Boolean collectFlowFlag, Boolean comprehensiveFlag) {
        ByteBuf byteBuf = buf.readBytes(3);
        BigDecimal value = HexUtils.byteBuf2BigDecimal(byteBuf, 1, true, false);
        JSONObject jsonObject = ControllerAreaFunctionCode.covertMetadata(name,null);
        properties.add(jsonObject);
        HashMap<String, Object> map = new HashMap<>();
        ControllerAreaFunctionCode code = ControllerAreaFunctionCode.fromName(name);
        map.put(code.name(), value);
        return map;
    }
}
