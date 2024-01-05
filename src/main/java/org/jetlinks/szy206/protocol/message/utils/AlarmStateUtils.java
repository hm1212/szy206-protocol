package org.jetlinks.szy206.protocol.message.utils;

import io.netty.buffer.ByteBuf;
import org.jetlinks.core.utils.BytesUtils;
import org.jetlinks.szy206.protocol.message.state.TerminalAlarmState;

import java.util.*;

/**
 * 终端报警状态解析工具
 */
public class AlarmStateUtils {

    /**
     * 逐位解析
     *
     * @param bytebuf
     * @return
     */

    public static HashMap<String, ArrayList<HashMap<String, Integer>>> resolveBit(ByteBuf bytebuf,
                                                                                  String name) {
        byte[] targetBytes = new byte[bytebuf.readableBytes()];
        ArrayList<HashMap<String, Integer>> list = new ArrayList<>();
        HashMap<String, ArrayList<HashMap<String, Integer>>> resultMap = new HashMap<>();
        HashMap<String, Integer> map = new HashMap<>();
        bytebuf.readBytes(targetBytes, 0, targetBytes.length);
        int num = BytesUtils.beToInt(targetBytes);
        Arrays.sort(TerminalAlarmState.values());
        for (TerminalAlarmState value : TerminalAlarmState.values()) {
            int bit = num & 0x1;
            if (!(value.name().startsWith("back"))) {
                map.put(value.name(), bit);
            }
            num = num >> 1;
        }
        list.add(map);
        resultMap.put(name, list);
        return resultMap;
    }
}
