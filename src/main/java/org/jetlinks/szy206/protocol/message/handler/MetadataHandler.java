package org.jetlinks.szy206.protocol.message.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.message.DerivedMetadataMessage;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.szy206.protocol.message.additional.Szy206Header;
import org.jetlinks.szy206.protocol.message.utils.Szy206MessageUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataHandler {

    public static Flux<DeviceMessage> toDeviceMessage(DeviceOperator operator, List<DeviceMessage> messageList,
                                                      JSONObject metadataObject) {
        return Flux.fromIterable(messageList)
                   .concatMap(message -> {
                       Szy206MessageUtils.addSL651MessageHeader(Szy206Header.getSzy206Header(), message);
                       if (message instanceof ReportPropertyMessage) {
                           return operator.getMetadata().map(metadata -> {
                               ArrayList<String> listPro = new ArrayList<>();
                               Map<String, Object> properties = ((ReportPropertyMessage) message).getProperties();
                               List<String> list = metadata
                                   .getProperties()
                                   .stream()
                                   .map(PropertyMetadata::getId)
                                   .collect(Collectors.toList());//原始物模型
                               if (!(properties == null)) {
                                   for (String f : properties.keySet()) {
                                       if (!list.contains(f)) {
                                           listPro.add(f);
                                       }
                                   }
                               }
                               return listPro;
                           }).flatMapMany(listPro -> {
                               ArrayList<DeviceMessage> messageArrayList = new ArrayList<>();
                               // 对比物模型
                               if (listPro.size() != 0) {
                                   System.out.println("listPro=====" + listPro);
                                   //自动创建物模型
                                   JSONArray properties = metadataObject.getJSONArray("properties");
                                   JSONArray newProperties = new JSONArray();
                                   Iterator<Object> iterator = properties.stream().iterator();
                                   while (iterator.hasNext()) {
                                       JSONObject property = (JSONObject) iterator.next();
                                       String id = property.getString("id");
                                       if (listPro.contains(id)) {
                                           newProperties.add(property);
                                       }
                                   }
                                   metadataObject.put("properties", newProperties);
                                   //自动创建物模型
                                   if (newProperties.size() > 0) {
                                       DerivedMetadataMessage metadataMessage = new DerivedMetadataMessage();
                                       metadataMessage.setAll(false);//增量传输物模型
                                       metadataMessage.setMetadata(metadataObject.toJSONString());
                                       metadataMessage.setDeviceId(message.getDeviceId());
                                       metadataMessage.setTimestamp(System.currentTimeMillis());
                                       messageArrayList.add(metadataMessage);
                                   }
                               }
                               messageArrayList.add(message);
                               return Flux.concat(Flux.fromIterable(messageArrayList));
                           });
                       }
                       return Flux.just(message);
                   });
    }
}