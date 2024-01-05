package org.jetlinks.szy206.protocol.message.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import org.apache.commons.codec.binary.Hex;
import org.jetlinks.core.utils.BytesUtils;
import org.jetlinks.szy206.protocol.message.binary.ComprehensiveValue;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 报文综合工具类
 */
public class HexUtils {
    /**
     * CRC检验码
     * X^7 + X^6 + X^5 + X^2 + 1*X^0
     * @param payload
     * @return
     */
    public static int getCrc8(ByteBuf payload) {
        ByteBuf duplicate = payload.duplicate();
        duplicate.resetReaderIndex();
        byte[] data = new byte[duplicate.readableBytes()];
        duplicate.readBytes(data);
        // CRC8 多项式系数
        final byte polynomial = (byte)229;//1110 0101
        byte crc = 0;
        for (byte b : data) {
            crc ^= b;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x80) != 0) {
                    crc = (byte) ((crc << 1) ^ polynomial);
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc;
    }

    /**
     * 解析TP参数值
     *
     * @param buf
     * @param properties
     * @return
     */
    public static HashMap<String, Object> resolveTp(ByteBuf buf, JSONArray properties) {
        HashMap<String, Object> resultMap = new HashMap<>();
        int seconds = buf.readByte();  //秒
        int sunit = (seconds) & 0xF;   //秒个位数
        int sten = (seconds >> 4) & 0xF;//秒十位数
        seconds = sten * 10 + sunit;
        int minute = buf.readByte();   //分
        int munit = (minute) & 0xF;
        int mten = (minute >> 4) & 0xF;
        minute = mten * 10 + munit;
        int hour = buf.readByte();     //时
        int hunit = (hour) & 0xF;
        int hten = (hour >> 4) & 0xF;
        hour = hten * 10 + hunit;
        int day = buf.readByte();      //日
        int dunit = (day) & 0xF;
        int dten = (day >> 4) & 0xF;
        day = dten * 10 + dunit;
        int delay = buf.readByte();   //延时时长
        int deunit = (delay) & 0xF;
        int deten = (delay >> 4) & 0xF;
        delay = deten * 10 + deunit;
        String data = String.format("%02d%02d%02d%02d", day, hour, minute, seconds);
        if (delay > 0) {
            long currentTimeMillis = System.currentTimeMillis();
            // 将当前时间和data转换为LocalDateTime对象
            LocalDateTime currentTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis), ZoneId.systemDefault());
            //构建时间年月为今年
            int currentYear = Year.now().getValue();
            int currentMonth = YearMonth.now().getMonthValue();
            String month = String.valueOf(currentMonth);
            if (currentMonth < 10) {
                month = 0 + month;
            }
            String newData = currentYear + "" + month + data;
            LocalDateTime targetTime = LocalDateTime.parse(newData, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            // 计算时间差
            Duration duration = Duration.between(currentTime, targetTime);
            long differenceInSeconds = duration.getSeconds();
            if (Math.abs(differenceInSeconds) > delay) {
                resultMap.put("timeOut", true); //传输超时
            }
        }
        resultMap.put("time", data);
        JSONObject jsonObject = covertMetadata("tp", "解析时间", "String");
        properties.add(jsonObject);
        return resultMap;
    }

    /**
     * 转换物模型
     *
     * @param id
     * @param name
     * @param dataType
     * @return
     */
    public static JSONObject covertMetadata(String id, String name, String dataType) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        JSONObject valueType = new JSONObject();
        valueType.put("type", dataType);
        jsonObject.put("valueType", valueType);
        JSONObject expands = new JSONObject();
        expands.put("readOnly", "true");
        expands.put("source", "device");
        jsonObject.put("expands", expands);
        return jsonObject;
    }

    /**
     * bytes转BigDecimal
     *
     * @param buf     数据
     * @param decimal 小数位
     * @param reverse 是否反转
     * @param PN      是否带+ -符号
     * @return float
     */
    public static BigDecimal byteBuf2BigDecimal(ByteBuf buf, int decimal, boolean reverse, boolean PN) {
        StringBuilder builder = new StringBuilder();//存放数据
        ByteBuf firstByteBuf = buf.readBytes(buf.readableBytes() - 1);
        String first = Hex.encodeHexString(BytesUtils.reverse(firstByteBuf.array()));
        builder.insert(0, first);
        int second = buf.readByte();
        int highValue = second & 0xF;
        builder.insert(0, highValue);
        int last = second >> 4;
        if (last == 0xF) {
            String s = builder.toString();
            return new BigDecimal(s).multiply(BigDecimal.valueOf(Math.pow(10, -1 * decimal)));
        }
        int lastValue = last & 0x07;
        builder.insert(0, lastValue);
        int pn = lastValue >> 3;
        String s = builder.toString();
        BigDecimal multiply = new BigDecimal(s).multiply(BigDecimal.valueOf(Math.pow(10, -1 * decimal)));
        if (PN) {
            if (pn == 0) {
                return multiply;
            } else if (pn == 1) {
                return multiply.multiply(new BigDecimal(-1));
            }
        }
        return multiply;
    }


    /**
     * 获取综合数据位数据
     *
     * @param data 综合数据
     * @return
     */
    public static ComprehensiveValue getComprehensiveValue(int data) {
        ComprehensiveValue comprehensiveValue = new ComprehensiveValue();
        ArrayList<Integer> properties = comprehensiveValue.getProperties();
        for (int i = 0; i < properties.size(); i++) {
            properties.add(data & 0x01);
            data = data >> 1;
        }
        return comprehensiveValue;
    }

    public static boolean checkValue(ByteBuf buf) {
        int length = buf.readableBytes();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        int first = bytes[0];
        int second = bytes[1];
        String hexString = Hex.encodeHexString(bytes).toUpperCase();
        if (first >= 10 || second >= 10) {
            return false;
        }
        if (hexString.startsWith("AAAA")) {
            return false;
        }
        return true;
    }
}
