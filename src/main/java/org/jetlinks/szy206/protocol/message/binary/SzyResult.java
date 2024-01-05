package org.jetlinks.szy206.protocol.message.binary;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.szy206.protocol.message.additional.Szy206Header;
import org.jetlinks.szy206.protocol.message.binary.images.Szy206ImageData;

import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
public class SzyResult {
    private JSONObject metadata;
    private ByteBuf ask;
    private List<DeviceMessage> messages = new ArrayList<>();

    private Szy206ImageData szy206ImageData;

    private Szy206Header szy206Header;

    public static SzyResult build() {
        return new SzyResult();
    }

    public void setAsk(ByteBuf ask) {
        this.ask = ask;
    }

    public ByteBuf getAsk() {
        return this.ask;
    }

    public JSONObject getMetadata() {
        return metadata;
    }

    public void setMetadata(JSONObject metadata) {
        this.metadata = metadata;
    }

    public List<DeviceMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<DeviceMessage> messages) {
        this.messages = messages;
    }

    public Szy206ImageData getSzy206ImageData() {
        return szy206ImageData;
    }

    public void setSzy206ImageData(Szy206ImageData szy206ImageData) {
        this.szy206ImageData = szy206ImageData;
    }

    public Szy206Header getSzy206Header() {
        return szy206Header;
    }

    public void setSzy206Header(Szy206Header szy206Header) {
        this.szy206Header = szy206Header;
    }
}



