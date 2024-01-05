package org.jetlinks.szy206.protocol.message.binary;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class ComprehensiveValue {
//    private int D0;//水质
//    private int D1;//土壤含水率
//    private int D2;//功率
//    private int D3;//风速（风向）
//    private int D4;//闸位
//    private int D5;//流量（水量）
//    private int D6;//水位
//    private int D7;//雨量

    private ArrayList<Integer> properties;

    public ComprehensiveValue() {
        properties = new ArrayList<>(8);
        for (int i = 0; i < properties.size(); i++) {
            properties.add(0);
        }

    }
}
