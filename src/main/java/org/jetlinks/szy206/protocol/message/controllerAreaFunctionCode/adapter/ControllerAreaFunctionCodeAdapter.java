package org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.adapter;

import com.alibaba.fastjson.JSONArray;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;

public interface ControllerAreaFunctionCodeAdapter {
    public HashMap<String,Object> ControllerAreaRead(ByteBuf buf, JSONArray properties,Boolean collectFlowFlag,Boolean comprehensiveFlag);
}
