package org.jetlinks.szy206.protocol.message.additional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@AllArgsConstructor
@NoArgsConstructor
public class Szy206Header {
    public static String SNO = "SNO";//序号
    public static String functionCode = "functionCode";//功能码
    public static String STTP = "STTP";//遥测站分类码

    public static String FTM = "FTM";//发送时间
    public static String GTM = "GTM";//数据时间

    public static String IP = "IP";//网络地址
    public static String PORT = "PORT";//网络端口

    @Getter
    @Setter
    HashMap<String, String> map = new HashMap<>();

    public static Szy206Header getSzy206Header() {
        return new Szy206Header();
    }

    public void populationSNo(String SNO) {
        SNO = SNO.toUpperCase();
        this.getMap().put(Szy206Header.SNO, SNO);
    }

    public void populationFunctionCode(String functionCode) {
        getMap().put(Szy206Header.functionCode, functionCode);
    }

    public void populationSTTP(String STTP) {
        getMap().put(Szy206Header.STTP, STTP.toUpperCase());
    }

    public void populationIP(String IP) {
        getMap().put(Szy206Header.IP, IP);
    }

    public void populationPORT(int PORT) {
        getMap().put(Szy206Header.PORT, String.valueOf(PORT));
    }

}
