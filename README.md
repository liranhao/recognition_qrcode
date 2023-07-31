# recognition_qrcode

识别图片中的二维码的flutter插件，简单好用
 ![](./demo.gif)
## Getting Started
采用GoogleMLKit框架，支持识别条形码、二维码等，支持识别包含多个二维码、条形码的图片
### 使用方法
```
//如果使用默认配置可以不配置config
 RecognitionQrcode.config(
      // icon: "assets/bx-right-arrow.png", //箭头图标: 传路径
      iconWidth: 30, // 箭头图标大小
      iconHeight: 30,// 箭头图标大小
      cancelTitleFontSize: 16, // 右上角取消按钮文字大小
      cancelTitle: "取消",// 右上角取消按钮文字
  );
//参数 img: 支持base64、url、filePath三种方式
RecognitionQrcode.recognition(
                        "https://tool.oschina.net/action/qrcode/generate?data=1231231231&output=image%2Fpng&error=L&type=0&margin=7&size=4.png").then((result) {
    print("recognition: $result");
  }).catchError((onError) {
    print("catchError:$onError");
});
```
由于googleMLKit的原因，不支持iOS模拟器上运行
