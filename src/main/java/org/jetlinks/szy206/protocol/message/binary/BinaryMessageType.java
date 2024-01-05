package org.jetlinks.szy206.protocol.message.binary;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.bouncycastle.util.encoders.Hex;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DeviceOfflineMessage;
import org.jetlinks.core.message.HeaderKey;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.szy206.protocol.message.binary.images.Szy206ImageData;
import org.jetlinks.szy206.protocol.message.function.downStream.RealTimeDataFunction;
import org.jetlinks.szy206.protocol.message.function.downStream.TerminalImageDataFunction;
import org.jetlinks.szy206.protocol.message.function.upStream.HeartReport;
import org.jetlinks.szy206.protocol.message.function.upStream.RealTimeDataReply;
import org.jetlinks.szy206.protocol.message.function.upStream.ReportRealTimeData;
import org.jetlinks.szy206.protocol.message.function.upStream.TerminalImageDataFunctionReply;
import org.jetlinks.szy206.protocol.message.utils.ControllerAreaUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
public enum BinaryMessageType {

    Heart(EventMessage.class, HeartReport::new, 0x02, "链路监测", true),

    RealTimeData(FunctionInvokeMessage.class, RealTimeDataFunction::new, 0xB0, "查询遥测终端实时值", false),

    RealTimeDataReply(ReportPropertyMessage.class, RealTimeDataReply::new, 0xB0, "查询遥测终端实时值回复解析", true),

    ReportRealTimeData(ReportPropertyMessage.class, ReportRealTimeData::new, 0xC0, "自报实时数据", true),

    TerminalImageData(FunctionInvokeMessage.class, TerminalImageDataFunction::new, 0x61, "查询遥测终端图像记录", false), TerminalImageDataReply(FunctionInvokeMessage.class, TerminalImageDataFunctionReply::new, 0x61, "查询遥测终端图像记录(回复报文)", true);

//    TerminalData(ReadPropertyMessage.class, BinaryReportPropertyMessage::new, 0xB1, "查询遥测终端固态存储数据"),
    // queryImageRecords(FunctionInvokeMessage.class, TerminalImageDataFunction::new, 0x61, "遥测终端自报实时数据"),
//    ReportAlarmData(ReadPropertyMessage.class, BinaryReportPropertyMessage::new, 0x81, "随机自报报警数据"),
//    ManualSetData(ReadPropertyMessage.class, BinaryReportPropertyMessage::new, 0x82, "人工置数");
    ;

    private final Class<? extends DeviceMessage> forDevice;

    private final Supplier<BinaryMessage<DeviceMessage>> forTcp;
    private final int functionCode;

    private final boolean upStream;


    private final String functionName;

    public static final HeaderKey<Integer> HEADER_MSG_SEQ = HeaderKey.of("_seq", 0, Integer.class);


    BinaryMessageType(Class<? extends DeviceMessage> forDevice, Supplier<? extends BinaryMessage<?>> forTcp, int functionCode, String functionName, Boolean upStream) {
        this.forDevice = forDevice;
        this.forTcp = (Supplier) forTcp;
        this.functionCode = functionCode;
        this.functionName = functionName;
        this.upStream = upStream;
    }

    public static SzyResult read(ByteBuf data, Boolean collectFlowFlag) {
        return read(data, collectFlowFlag, (szyResult) -> {
            List<DeviceMessage> messages = szyResult.getMessages();
            JudgingOfflineMessage(szyResult, messages);//离线
            addSubData(szyResult, messages);//图片
            return szyResult;
        });
    }

    private static void JudgingOfflineMessage(SzyResult szyResult, List<DeviceMessage> messages) {
        messages
            .stream()
            .filter(message -> message instanceof EventMessage)
            .map(message -> {
                return (EventMessage) message;
            })
            .findFirst()
            .ifPresent(eventMessage -> {
                HashMap map = (HashMap) eventMessage.getData();
                Object obj = map.get(HeartReport.HEARTDATA);
                if (!Objects.isNull(obj)) {
                    String data = (String) obj;
                    if (HeartReport.HeartEnum.F1.getDescribe().equalsIgnoreCase(data)) {
                        DeviceOfflineMessage deviceOfflineMessage = new DeviceOfflineMessage();
                        deviceOfflineMessage.setDeviceId(eventMessage.getDeviceId());
                        szyResult.getMessages().add(deviceOfflineMessage);
                    }
                }
            });
    }


    private static void addSubData(SzyResult szyResult, List<DeviceMessage> messages) {
        messages
            .stream()
            .filter(message -> message instanceof ReportPropertyMessage)
            .map(message -> {
                return (ReportPropertyMessage) message;
            })
            .findFirst()
            .ifPresent(reportPropertyMessage -> {
                Map<String, Object> properties = reportPropertyMessage.getProperties();
                Object obj = properties.get(TerminalImageDataFunctionReply.imageHexKey);
                if (!Objects.isNull(obj)) {
                    String hex = (String) obj;
                    szyResult.getSzy206ImageData().setSubData(Hex.decode(hex));
                }
            });
    }


    public static ByteBuf write(DeviceMessage message, ByteBuf data) {
        return write(message, 0, data);
    }

    public static <T> T read(ByteBuf data, Boolean collectFlowFlag, Function<SzyResult, T> handler) {

        int counter = 0;
        int len = data.readableBytes();
        int start0 = data.readByte();//起始字符0x68

        int length = data.readByte();//1 个字节长度 L

        int start1 = data.readByte();//起始字符0x68

        int controllerArea = data.readByte();//控制域
        length--;

        ControllerAreaUtils.getDir(controllerArea);//判断报文上下行  下行报文直接抛异常

        int divs = 0;//拆分帧计数 DIVS
        SzyResult szyResult = SzyResult.build();
        if (ControllerAreaUtils.getDiv(controllerArea) == 1) {
            divs = data.readByte();
            if (Objects.isNull(szyResult.getSzy206ImageData())) {
                szyResult.setSzy206ImageData(new Szy206ImageData());
            }
            szyResult.getSzy206ImageData().setDivs(divs);
            length--;
            counter++;
        }
        int fcb = ControllerAreaUtils.getFCB(controllerArea);

        int areaFunctionCode = ControllerAreaUtils.getFunctionCode(controllerArea);

        byte[] addressAreaBytes = new byte[5];

        data.readBytes(addressAreaBytes);//地址域
        length -= 5;

        int functionCode = Byte.toUnsignedInt(data.readByte());//应用层功能码
        length--;
        if (length < 0) {//图片数据，l=01;
            length = len - 10 - counter;
        }
        BinaryMessageType binaryMessageType = getBinaryMessageType(functionCode, true);

        BinaryMessage<DeviceMessage> forTcp = binaryMessageType.forTcp.get();

        forTcp.read(data, length, addressAreaBytes, areaFunctionCode, collectFlowFlag);
        DeviceMessage message = forTcp.getMessage();
        if (message != null) {
            ByteBuf sourceAskPayload = forTcp.getAskPayload();
            JSONObject metadata = forTcp.getMetadata();
            ByteBuf ask = buildAsk(start0, start1, sourceAskPayload);
            List<DeviceMessage> messageList = szyResult.getMessages();
            messageList.clear();
            messageList.add(message);
            szyResult.setMessages(messageList);
            szyResult.setMetadata(metadata);
            szyResult.setAsk(ask);
            return handler.apply(szyResult);
        }
        return handler.apply(szyResult);
    }

    private static ByteBuf buildAsk(int start0, int start1, ByteBuf payload) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(start0);
        buffer.writeByte(payload.readableBytes() - 1);
        buffer.writeByte(start1);
        buffer.writeBytes(payload, payload.readerIndex(), payload.readableBytes());
        buffer.writeByte(0x16);
        return buffer;
    }

    public static ByteBuf write(DeviceMessage message, int msgId, ByteBuf data) {
        if (message instanceof FunctionInvokeMessage) {
            FunctionInvokeMessage functionInvokeMessage = (FunctionInvokeMessage) message;

            BinaryMessageType type = lookup(functionInvokeMessage);

            // 创建消息对象
            BinaryMessage<DeviceMessage> tcp = type.forTcp.get();

            //写出数据到ByteBuf
            tcp.write(data, message);

            return data;
        }
        throw new UnsupportedOperationException("unsupported function message " + message);
    }

    public static BinaryMessageType lookup(FunctionInvokeMessage message) {
        for (BinaryMessageType value : values()) {
            if (value.forDevice != null
                && value.forDevice.isInstance(message)
                && value.upStream == false
                && Integer.toHexString(value.getFunctionCode()).equalsIgnoreCase(message.getFunctionId())) {
                return value;
            }
        }
        throw new UnsupportedOperationException("unsupported device message " + message.getMessageType());
    }


    /**
     * 根据功能码获取报文对应的功能
     *
     * @param functionCode 功能码
     * @return
     */
    public static BinaryMessageType getBinaryMessageType(int functionCode, boolean upStream) {
        for (BinaryMessageType value : values()) {
            if (value.functionCode == functionCode && upStream == value.upStream) {
                return value;
            }
        }
        throw new UnsupportedOperationException("不支持的功能码类型");
    }
}
