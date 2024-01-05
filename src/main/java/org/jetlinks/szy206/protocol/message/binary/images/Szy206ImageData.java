package org.jetlinks.szy206.protocol.message.binary.images;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Szy206ImageData {
    private int divs;//当前帧数

    private byte[] subData;//报文数据

    // 将 JSON 字符串转换为 PackageMessage 对象
    public static Szy206ImageData fromJsonString(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, Szy206ImageData.class);
        } catch (Exception e) {
            // 处理异常
            e.printStackTrace();
            return null;
        }
    }

    public String toJsonString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            // 处理异常
            e.printStackTrace();
            return null;
        }
    }


}
