# recognition_qrcode

识别图片中的二维码的flutter插件，简单好用

## Getting Started

### 使用方法
```
RecognitionQrcode.recognition(
                        "https://tool.oschina.net/action/qrcode/generate?data=1231231231&output=image%2Fpng&error=L&type=0&margin=7&size=4.png").then((result) {
    print("recognition: $result");
  });
```
参数 img: 支持base64、url、filePath三种方式
