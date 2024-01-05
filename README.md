# Szy206协议

## 获取代码

第一步: 先到个人设置中[添加SSH key](https://github.com/settings/keys)

第二步: 拉取代码

```bash
 $ git clone -b master  git@github.com:jetlinks-v2/SZY206-protocol.git
```

第三步: JetLinks-pro平台上传SZY206协议Jar包
```text
物联网-运维管理-协议管理-上传协议Jar包
```
第四步: JetLinks-pro平台新增网络组件
```text
物联网-运维管理-网络组件-新增网络组件(TCP服务类型)
```
粘拆包自定义脚本如下
```bash
parser
  .fixed(1)
  .handler((buf, _parser) => {
    if (buf.getByte(0) == 0x7e) {
      _parser.result(buf).fixed(1);
    } else {
      _parser.complete();
    }
  })
  .fixed(1)
  .handler((buf, _parser) => {
    if (buf.getByte(0) == 0x7e) {
      _parser.result(buf).fixed(11);
    } else {
      _parser.complete();
    }
  })
  .handler((buf, _parser) => {
    var len = buf.getShort(9);
    _parser
      .result(buf)
      .fixed(len + 4);
  })
  .handler((buf, _parser) => {
    _parser
      .result(buf)
      .complete();
  });
```
第五步: JetLinks-pro平台新增设备接入网关
```text
物联网-运维管理-设备接入网关-新增网关(TCP透传接入)
```
第五步: JetLinks-pro平台新增产品和设备
```text
物联网-设备管理-新增产品和设备
```

