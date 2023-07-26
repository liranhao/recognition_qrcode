
#import "RecognitionQrcodePlugin.h"
#import "ImageViewController.h"
#import "ZBarSDK.h"
#import "BarCodeObject.h"
#import "RecognitionConfig.h"
//#import <GoogleMLKit/MLKit.h>
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
            [self recognitionImage:image result:result];
        } else {
            result([FlutterError errorWithCode:@"-2" message:@"Image parsing failed" details:nil]);
        }
    } else {
        result(FlutterMethodNotImplemented);
    }
}


- (void) recognitionImage:(UIImage *)image result:(FlutterResult)result{
    
    NSMutableArray<BarCodeObject *> *array = [[NSMutableArray alloc]initWithCapacity:1];
    if(array.count == 0){
        CIDetector *detector = [CIDetector detectorOfType:CIDetectorTypeQRCode context:nil options:@{CIDetectorAccuracy: CIDetectorAccuracyHigh}];
            // 取得识别结果
        NSArray *detectorList = [detector featuresInImage:[CIImage imageWithCGImage:image.CGImage]];
        for(CIQRCodeFeature *feature in detectorList){

            CGPoint point = feature.bounds.origin;
    //        CGRectMake(point.x, point.y, feature.bounds.size.width, feature.bounds.size.height)
            point.x = image.size.width - feature.bounds.origin.x;
            BarCodeObject *barcode = [[BarCodeObject alloc] initBounds:CGRectMake(feature.bounds.origin.x,   image.size.height - feature.bounds.origin.y - feature.bounds.size.height, feature.bounds.size.width, feature.bounds.size.height) value:feature.messageString];
            [array addObject:barcode];
        }
    }
    if(array.count == 0 ){
        ZBarReaderController *read = [ZBarReaderController new];
        read.maxScanDimension = 2000000;
        CGImageRef cgImageRef = image.CGImage;
        ZBarSymbol* symbol = nil;
        for(symbol in [read scanImage:cgImageRef]){
            BarCodeObject *barcode = [[BarCodeObject alloc] initBounds:symbol.bounds value:symbol.data];
            [array addObject:barcode];
        }
    }
    
    NSArray *features = array;
    if(features.count == 0){
        result([FlutterError errorWithCode:@"-1" message:@"No results" details:nil]);
    }else if (features.count == 1){
        BarCodeObject *barcode = [features objectAtIndex:0];
        result(@{@"code": @"0", @"value": barcode.value});
    } else if (features.count > 0) {
        UIViewController *controller = [UIApplication sharedApplication].delegate.window.rootViewController;
        ImageViewController *viewController = [[ImageViewController alloc] init];
        viewController.image = image;
        viewController.barcodes = features;
        //解析结果回调
        viewController.clickBarCodeFinish = ^(NSString * _Nonnull value){
          result(@{@"code": @"0", @"value": value});
        };
        viewController.modalPresentationStyle = UIModalPresentationFullScreen;
        [controller presentViewController:viewController animated:true completion:nil];
    }
}
@end

