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

public class WaterQuality implements ControllerAreaFunctionCodeAdapter {
    private static String name = "waterQuality";

    @Override
    public HashMap<String, Object> ControllerAreaRead(ByteBuf buf, JSONArray properties, Boolean collectFlowFlag, Boolean comprehensiveFlag) {

        JSONObject jsonObject = ControllerAreaFunctionCode.covertMetadata(name,"array");
        properties.add(jsonObject);
        ControllerAreaFunctionCode code = ControllerAreaFunctionCode.fromName(name);
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<Object> list = new ArrayList<>();
        int length = buf.readableBytes();
        while (length != buf.readerIndex()) {
            ByteBuf byteBuf = buf.readBytes(3);
            BigDecimal value = HexUtils.byteBuf2BigDecimal(byteBuf, 0, true, true);
            list.add(value);
            if (comprehensiveFlag) {
                break;
            }
        }
        map.put(code.name(), list);
        return map;
    }
}
