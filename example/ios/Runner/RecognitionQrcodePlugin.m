#import "RecognitionQrcodePlugin.h"
//#import <ImageCaptureCore/ImageCaptureCore.h>
@implementation RecognitionQrcodePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"recognition_qrcode"
            binaryMessenger:[registrar messenger]];
  RecognitionQrcodePlugin* instance = [[RecognitionQrcodePlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }else if([@"recognitionQrcode" isEqualToString: call.method]){
      id arguments = call.arguments;
      UIImage *image;
      if([arguments isKindOfClass: [UIImage class]]){
          image = call.arguments;
      } else if([arguments isKindOfClass: [NSString class]]){
          if([arguments containsString: @"http://"] || [arguments containsString: @"https://"]){
              image = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:arguments]]];
          } else if([arguments containsString: @"file://"]){
              image = [UIImage imageWithContentsOfFile:arguments];
          } else {
              NSData *imageData =[[NSData alloc] initWithBase64EncodedString:arguments options:NSDataBase64DecodingIgnoreUnknownCharacters];
              if(imageData){
                  image = [UIImage imageWithData:imageData];
              }
          }
      }
      if(image){
          CIDetector *detector = [CIDetector detectorOfType:CIDetectorTypeQRCode context:nil options:@{CIDetectorAccuracy: CIDetectorAccuracyHigh}];
              // 取得识别结果
          NSArray *features = [detector featuresInImage:[CIImage imageWithCGImage:image.CGImage]];
          if (features.count == 0) {
              result([FlutterError errorWithCode:@"-1" message:@"No results" details:nil]);
              return;
          } else {
              CIQRCodeFeature *feature = [features objectAtIndex:0];
              NSLog(@"读取二维码数据信息2 - - %@", feature.messageString);
              result(@{@"code": @"0", @"value": feature.messageString});
          }
      } else {
          result([FlutterError errorWithCode:@"-2" message:@"Image parsing failed" details:nil]);
      }
  }else {
    result(FlutterMethodNotImplemented);
  }
}

@end

