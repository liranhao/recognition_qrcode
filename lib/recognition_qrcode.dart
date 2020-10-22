import 'dart:async';

import 'package:flutter/services.dart';

class RecognitionQrcode {
  static const MethodChannel _channel =
      const MethodChannel('recognition_qrcode');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<Map> recognitionQrcode(dynamic img) async {
    var result = await _channel.invokeMethod('recognitionQrcode', img);
    return result;
  }
}
