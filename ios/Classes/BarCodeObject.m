//
//  BarCodeObject.m
//  recognition_qrcode
//
//  Created by 李然豪 on 2023/7/24.
//

#import "BarCodeObject.h"

@implementation BarCodeObject
-(instancetype)initBounds:(CGRect)bounds value:(NSString *)value{
    if(self = [super init]){
        _value = value;
        _bounds = bounds;
    }
    return self;
}
@end
