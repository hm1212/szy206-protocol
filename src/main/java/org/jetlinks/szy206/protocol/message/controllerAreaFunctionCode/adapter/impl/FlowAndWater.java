package org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.adapter.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import org.apache.commons.codec.binary.Hex;
import org.jetlinks.core.utils.BytesUtils;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.ControllerAreaFunctionCode;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.adapter.ControllerAreaFunctionCodeAdapter;
import org.jetlinks.szy206.protocol.message.utils.HexUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;


//0x02
public class FlowAndWater implements ControllerAreaFunctionCodeAdapter {

    private static String name = "flowAndWater";

    @Override
    public HashMap<String, Object> ControllerAreaRead(ByteBuf buf, JSONArray properties, Boolean collectFlowFlag, Boolean comprehensiveFlag) {

        int length = buf.readableBytes();
        HashMap<String, Object> map = new HashMap<String, Object>();
        ArrayList<Object> list = new ArrayList<>();
        ControllerAreaFunctionCode code = ControllerAreaFunctionCode.fromName(name);
        JSONObject jsonObject = ControllerAreaFunctionCode.covertMetadata(name, "array");
        properties.add(jsonObject);
        while (length != buf.readerIndex()) {
            if (collectFlowFlag) { //读取流量+水量
                ByteBuf flowBuf = buf.readBytes(5);
                boolean b = HexUtils.checkValue(flowBuf);
                if (b) {
                    BigDecimal flow = HexUtils.byteBuf2BigDecimal(flowBuf, 3, true, true);
                    list.add(flow);
                } else {
                    list.add(Hex.encodeHexString(flowBuf.array()));
                }
                ByteBuf waterBuf = buf.readBytes(5);
                b = HexUtils.checkValue(flowBuf);
                if (b) {
                    BigDecimal water = byteBuf2Float(waterBuf, 0, true);
                    list.add(water);
                } else {
                    list.add(Hex.encodeHexString(flowBuf.array()));
                }
                if (comprehensiveFlag) {
                    break;
                }
            } else {
                ByteBuf flowBuf = buf.readBytes(5);
                boolean b = HexUtils.checkValue(flowBuf);
                if (b) {
                    BigDecimal flow = HexUtils.byteBuf2BigDecimal(flowBuf, 3, true, true);
                    list.add(flow);
                } else {
                    list.add(Hex.encodeHexString(flowBuf.array()));
                }
                if (comprehensiveFlag) {
                    break;
                }
            }
        }

        map.put(code.name(), list);
        return map;
    }


    /**
     * bytes转float
     *
     * @param buf     数据
     * @param decimal 小数位
     * @param reverse 是否反转
     * @return float
     */
    public static BigDecimal byteBuf2Float(ByteBuf buf, int decimal, boolean reverse) {
        byte[] bytes = new byte[buf.readableBytes() - 1];
        buf.readBytes(bytes);
        if (reverse) {
            BytesUtils.reverse(bytes);
        }
        StringBuilder builder = new StringBuilder();//存放数据
        for (int i = bytes.length - 1; i >= 0; i--) {
            int b = bytes[i];
            for (int j = 0; j < 2; j++) {
                if (decimal > 0 && builder.length() == decimal) {
                    builder.insert(0, ".");
                }
                int value = b & 0xF;
                builder.insert(0, value);
                b = b >> 4;
            }
        }
        byte b = buf.getByte(buf.readableBytes());
        int highValue = b & 0xF;
        builder.insert(0, highValue);
        int c = (b >> 4) & 0xF;
        int lastData = c & 0x07;
        int PN = (c >> 3) & 0x01;
        builder.insert(0, lastData);
        if (PN == 0) {
            return new BigDecimal(builder.toString());
        }
        return new BigDecimal(builder.toString()).multiply(BigDecimal.valueOf(Math.pow(10, -1)));
    }
}
