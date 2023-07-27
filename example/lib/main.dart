import 'dart:ffi';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:image_picker/image_picker.dart';
import 'package:recognition_qrcode/recognition_qrcode.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  RecognitionResult? result;
  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;
    RecognitionManager.setConfig(
      // icon: "assets/bx-right-arrow.png",
      iconWidth: 30,
      iconHeight: 30,
      cancelTitleFontSize: 16,
      cancelTitle: "取消",
    );
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              CupertinoButton(
                  child: Text("识别图片"),
                  onPressed: () {
                    final picker = ImagePicker();
                    picker
                        .pickImage(source: ImageSource.gallery)
                        .then((XFile? value) {
                      if (value == null) {
                        return;
                      }
                      RecognitionManager.recognition(value.path).then((result) {
                        print("RecognitionQrcode: $result");
                        setState(() {
                          this.result = result;
                        });
                      }).catchError((onError) {
                        print("catchError:$onError");
                      });
                    }); //
                  }),
              Text(result?.value ?? ""),
            ],
          ),
        ),
      ),
    );
  }
}
