package org.jetlinks.szy206.protocol.message.handler;

import org.apache.commons.codec.digest.DigestUtils;

public class Md5Utils {

    public static String Md5Hex(String deviceId, int divs) {
        String key = deviceId + "|" + divs;
        String md5Key = DigestUtils.md5Hex(key);
        return md5Key;
    }

}
