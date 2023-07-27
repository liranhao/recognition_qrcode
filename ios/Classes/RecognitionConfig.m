//
//  RecognitionConfig.m
//  recognition_qrcode
//
//  Created by 李然豪 on 2023/7/26.
//

#import "RecognitionConfig.h"

@implementation RecognitionConfig

static RecognitionConfig *instance = nil;
- (void)setConfig:(NSDictionary *)config{
    if([config[@"iconWidth"] isKindOfClass: [NSNumber class]]){
        NSNumber *size = config[@"iconWidth"];
        self.iconWidth = size.doubleValue;
    }
    if([config[@"iconHeight"] isKindOfClass: [NSNumber class]]){
        NSNumber *size = config[@"iconHeight"];
        self.iconHeight = size.doubleValue;
    }
    if([config[@"cancelTitleFontSize"] isKindOfClass: [NSNumber class]]){
        NSNumber *size = config[@"cancelTitleFontSize"];
        self.cancelTitleFontSize = size.doubleValue;
    }
    if([config[@"cancelTitle"] isKindOfClass:[NSString class]]){
        self.cancelTitle = config[@"cancelTitle"];
    }
//    if([config[@"backgroundColor"] isKindOfClass:[NSString class]]){
//        self.backgroundColor = config[@"backgroundColor"];
//    }
    if(config[@"icon"]){
        self.icon = config[@"icon"];
    }
}
+ (instancetype)shareInstance {
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (instance == nil) {
            instance = [[RecognitionConfig alloc] init];
        }
    });
    return instance;
}

+ (instancetype)allocWithZone:(struct _NSZone *)zone {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
         instance = [super allocWithZone:zone];
    });
   return instance;
}
- (id)copyWithZone:(nullable NSZone *)zone {
    return instance;
}
@end
