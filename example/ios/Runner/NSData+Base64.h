//
//  NSData+Base64.h
//  Runner
//
//  Created by 李然豪(LiRanHao) on 2020/10/19.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSData (Base64)
- (instancetype) initWithBase64EncodedString:(NSString *)base64String;

/**
 Create a Base-64 encoded NSString from the receiver's contents
 @returns A Base-64 encoded NSString
 */
- (NSString *) base64EncodedString;
@end

NS_ASSUME_NONNULL_END
