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

//0x04
public class Speed implements ControllerAreaFunctionCodeAdapter {

    private static String name = "speed";

    @Override
    public HashMap<String, Object> ControllerAreaRead(ByteBuf buf, JSONArray properties, Boolean collectFlowFlag, Boolean comprehensiveFlag) {
        int length = buf.readableBytes();
        HashMap<String, Object> map = new HashMap<>();
        JSONObject jsonObject = ControllerAreaFunctionCode.covertMetadata(name,"array");
        ArrayList<Object> list = new ArrayList<>();
        properties.add(jsonObject);
        while (length != buf.readerIndex()) {
            ByteBuf byteBuf = buf.readBytes(3);
            BigDecimal value = HexUtils.byteBuf2BigDecimal(byteBuf, 3, true, true);
            list.add(value);
        }
        ControllerAreaFunctionCode code = ControllerAreaFunctionCode.fromName(name);
        map.put(code.name(), list);
        return map;
    }
}
