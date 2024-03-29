import 'dart:async';

import 'dart:typed_data';
import 'package:flutter/services.dart';
import 'package:recognition_qrcode/src/recognition_result.dart';

class RecognitionManager {
  static const MethodChannel _channel =
      const MethodChannel('recognition_qrcode');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> setConfig({
    String? icon,
    double? iconWidth = 30,
    double? iconHeight = 30,
    double? cancelTitleFontSize = 16,
    String? cancelTitle = "取消",
  }) async {
    Map<String, dynamic> map = {
      "iconWidth": iconWidth ?? 30,
      "iconHeight": iconHeight ?? 30,
      "cancelTitleFontSize": cancelTitleFontSize ?? 16,
      "cancelTitle": cancelTitle ?? "取消",
    };
    try {
      if (icon != null) {
        ByteData byteData = await rootBundle.load(icon);
        ByteBuffer buffer = byteData.buffer;

        map["icon"] = Uint8List.view(buffer);
      }
    } catch (e) {
      print("RecognitionQrcode.config: Failed to get image");
    }
    await _channel.invokeMethod("setConfig", map);
  }

  // base64 || url || file path
  static Future<RecognitionResult> recognition(dynamic img) async {
    var res = await _channel.invokeMethod('recognitionQrcode', img);
    RecognitionResult result = RecognitionResult(
      code: res["code"],
      value: res["value"],
    );
    return result;
  }
}
