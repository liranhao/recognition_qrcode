import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:recognition_qrcode/recognition_qrcode.dart';

void main() {
  const MethodChannel channel = MethodChannel('recognition_qrcode');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await RecognitionQrcode.platformVersion, '42');
  });
}
