//
//  ImageViewController.h
//  recognition_qrcode
//
//  Created by 李然豪 on 2023/7/23.
//

#import <UIKit/UIKit.h>
#import "BarCodeObject.h"
NS_ASSUME_NONNULL_BEGIN

@interface ImageViewController : UIViewController
@property(strong, nonatomic) UIImage *image;
@property(strong, nonatomic) NSArray<BarCodeObject *>* barcodes;
@property(copy, nonatomic) void (^clickBarCodeFinish)(NSString *value);
@end

NS_ASSUME_NONNULL_END
