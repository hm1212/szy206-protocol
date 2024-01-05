package org.jetlinks.szy206.protocol.message.utils;

import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.szy206.protocol.message.additional.Szy206Header;
import org.jetlinks.szy206.protocol.message.controllerAreaFunctionCode.ControllerAreaFunctionCode;

import java.util.HashMap;
import java.util.Objects;

public class Szy206MessageUtils {

    public static void addSL651MessageHeader(Szy206Header szy206Header, DeviceMessage message) {
        if (!Objects.isNull(szy206Header)) {
            HashMap<String, String> map = szy206Header.getMap();
            for (String key : map.keySet()) {
                message.addHeader(key, map.get(key));
            }
        }
    }

    public static int getIntParameter(FunctionInvokeMessage functionInvokeMessage, String parameterName) {

        return functionInvokeMessage.getInputs().stream()
                                    .filter(functionParameter -> functionParameter
                                        .getName()
                                        .equalsIgnoreCase(parameterName))
                                    .mapToInt(functionParameter -> {
                                        return Integer.parseInt(String.valueOf((functionParameter.getValue())));
                                    })
                                    .findAny()
                                    .getAsInt();
    }

    /**
     * 封装控制域数据
     *
     * @param times
     * @param valueCode
     */
    public static int getC(int times, int valueCode) {
        int maxTimes = 3;
        int c = 0x30;

        times = Math.min(times, maxTimes);
        times <<= 4;
        c &= times;//确定高4位
        ControllerAreaFunctionCode controllerAreaFunctionCode = ControllerAreaFunctionCode.fromCode(valueCode);
        int code = controllerAreaFunctionCode.getCode();//低4位
        // 将低4位与 c 的高4位拼接成一个字节
        c |= code;
        return c;
    }
}
