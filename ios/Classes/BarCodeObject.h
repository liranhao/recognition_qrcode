//
//  BarCodeObject.h
//  recognition_qrcode
//
//  Created by 李然豪 on 2023/7/24.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BarCodeObject : NSObject
@property (readonly, nonatomic) CGRect bounds;
@property (readonly, nonatomic) NSString *value;

-(instancetype)initBounds:(CGRect )bounds value:(NSString *)value ;
@end

NS_ASSUME_NONNULL_END
