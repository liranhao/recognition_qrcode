
#import "RecognitionQrcodePlugin.h"
#import "ImageViewController.h"
#import "RecognitionConfig.h"
#if TARGET_IPHONE_SIMULATOR

#else
#import <GoogleMLKit/MLKit.h>
#endif
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
    } else if([call.method isEqualToString:@"setConfig"]){
        [RecognitionConfig.shareInstance setConfig:call.arguments];
    } else if([@"recognitionQrcode" isEqualToString: call.method]){
        id arguments = call.arguments;
        UIImage *image;
        if([arguments isKindOfClass: [UIImage class]]){
            image = call.arguments;
        } else if([arguments isKindOfClass: [NSString class]]){
            if([arguments containsString: @"http://"] || [arguments containsString: @"https://"]){
                image = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:arguments]]];
            } else {
                NSData *imageData = [[NSData alloc]initWithBase64EncodedString:arguments options:(NSDataBase64DecodingIgnoreUnknownCharacters)];;
                if(imageData){
                    image = [UIImage imageWithData:imageData];
                }
            }
            if(image == nil){
                image = [[UIImage alloc] initWithContentsOfFile: arguments];
            }
        }
        if(image){
            image = [UIImage imageWithCGImage:image.CGImage scale:image.scale orientation:UIImageOrientationUp];
#if TARGET_IPHONE_SIMULATOR
            result([FlutterError errorWithCode:@"-3" message:@"Please run this feature on a real device." details:nil]);
#else
            [self recognitionImage:image result:result];
#endif
        } else {
            result([FlutterError errorWithCode:@"-2" message:@"Image parsing failed" details:nil]);
        }
    } else {
        result(FlutterMethodNotImplemented);
    }
}


- (void) recognitionImage:(UIImage *)image result:(FlutterResult)result{
#if TARGET_IPHONE_SIMULATOR

#else
    MLKBarcodeScannerOptions *options =
      [[MLKBarcodeScannerOptions alloc]
       initWithFormats: MLKBarcodeFormatAll];
    
    MLKVisionImage *visionImage = [[MLKVisionImage alloc] initWithImage:image];
    visionImage.orientation = image.imageOrientation;
    MLKBarcodeScanner *barcodeScanner = [MLKBarcodeScanner barcodeScannerWithOptions: options];
    [barcodeScanner processImage:visionImage
                      completion:^(NSArray<MLKBarcode *> *_Nullable barcodes,
                                   NSError *_Nullable error) {
    if (error != nil) {
    // Error handling
        result([FlutterError errorWithCode:[NSString stringWithFormat:@"%ld", (long)error.code]  message:error.description details:nil]);
        return;
    }
    if(barcodes.count == 0){
        result(@{@"code": @"-1"});
    }else if (barcodes.count == 1){
        MLKBarcode *barcode = [barcodes objectAtIndex:0];
        result(@{@"code": @"0", @"value": barcode.rawValue});
    } else if (barcodes.count > 0) {
          UIViewController *controller = [UIApplication sharedApplication].delegate.window.rootViewController;
          ImageViewController *viewController = [[ImageViewController alloc] init];
          viewController.image = image;
          viewController.barcodes = barcodes;
          //解析结果回调
          viewController.clickBarCodeFinish = ^(NSString * _Nonnull value){
              result(@{@"code": @"0", @"value": value});
          };
          viewController.modalPresentationStyle = UIModalPresentationFullScreen;
          [controller presentViewController:viewController animated:true completion:nil];
      }
    }];
#endif
}
@end

