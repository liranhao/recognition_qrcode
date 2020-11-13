
#import "RecognitionQrcodePlugin.h"
#import "ZBarSDK.h"
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
            ZBarReaderController *controller = [ZBarReaderController new];
            
            id<NSFastEnumeration> results = [controller scanImage:image.CGImage];
            ZBarSymbol *symbol = nil;
            
            for (symbol in results) {
                break;
            }
            
            if (symbol.data) {
                result(@{@"code": @"0", @"value": symbol.data});
                return;
            } else {
                result([FlutterError errorWithCode:@"-1" message:@"No results" details:nil]);
            }
        } else {
            result([FlutterError errorWithCode:@"-2" message:@"Image parsing failed" details:nil]);
        }
    } else {
        result(FlutterMethodNotImplemented);
    }
}

@end
