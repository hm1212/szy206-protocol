package org.jetlinks.szy206.protocol.message.utils;

/**
 * 控制域解析工具
 */
public class ControllerAreaUtils {
    /**
     * D7            |D6           |D5～D4       |D3～D0
     * 传输方向位 DIR |拆分标志位 DIV | 帧计数位 FCB |功能码
     * DIR=0，表示此帧报文是由中心站发出的下行报文; 0xxx xxxx
     * DIR=1，表示此帧报文是由终端发出的上行报文;   1xxx xxxx
     *
     * @param data 控制域数据
     * @return
     */
    public static void getDir(int data) {
        if ((int) data > 0) {
            throw new UnsupportedOperationException("此报文非上行报文");
        }
    }

    /**
     * D7            |D6           |D5～D4       |D3～D0
     * 传输方向位 DIR |拆分标志位 DIV | 帧计数位 FCB |功能码
     * DIV =1，表示此报文已被拆分为若干帧，此时控制域 C 后增加一个字节，为拆分帧计数 DIVS，采用 BIN 倒计数（255～1），1 时表示最后一帧
     * DIV =0，表示此帧报文为单帧。
     *
     * @param data 控制域数据
     * @return
     */
    public static int getDiv(int data) {
        return (data >> 6) & 0x01;
    }

    /**
     * D7            |D6           |D5～D4       |D3～D0
     * 传输方向位 DIR |拆分标志位 DIV | 帧计数位   |功能码
     *
     * @param data 控制域数据
     * @return
     */
    public static int getFCB(int data) {
        return (data >> 4) & 0x03;
    }

    /**
     * D7            |D6           |D5～D4       |D3～D0
     * 传输方向位 DIR |拆分标志位 DIV | 帧计数位 FCB |功能码
     *
     * @param data 控制域数据
     * @return
     */
    public static int getFunctionCode(int data) {
        return data & 0xF;
    }

    /**
     * D7            |D6           |D5～D4       |D3～D0
     * 传输方向位 DIR |拆分标志位 DIV | 帧计数位 FCB |功能码
     *
     * @param data 控制域数据
     * @return
     */
    public static int setDir(int data) {
        return data & 0x7F;
    }

}
