//
//  RecognitionConfig.h
//  recognition_qrcode
//
//  Created by 李然豪 on 2023/7/26.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
NS_ASSUME_NONNULL_BEGIN

@interface RecognitionConfig : NSObject
@property (assign, nonatomic) double iconHeight;
@property (assign, nonatomic) double iconWidth;
@property (assign, nonatomic) double cancelTitleFontSize;
@property (strong, nonatomic) NSString *cancelTitle;
@property (strong, nonatomic) FlutterStandardTypedData *icon;
+ (instancetype)shareInstance;
- (void)setConfig:(NSDictionary *)config;
@end

NS_ASSUME_NONNULL_END
