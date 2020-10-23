//
//  NSData+Base64.m
//  Runner
//
//  Created by 李然豪(LiRanHao) on 2020/10/19.
//
#import "NSData+Base64.h"

@interface NSString (Base64)

- (NSString *) stringPaddedForBase64;

@end

@implementation NSString (Base64)

- (NSString *) stringPaddedForBase64 {
    NSUInteger paddedLength = self.length + (self.length % 3);
    return [self stringByPaddingToLength:paddedLength withString:@"=" startingAtIndex:0];
}

@end

@implementation NSData (Base64)

- (instancetype) initWithBase64EncodedString:(NSString *)base64String {
    return [self initWithBase64EncodedString:[base64String stringPaddedForBase64]];
}

- (NSString *) base64EncodedString {
    return [self base64EncodedStringWithOptions: NSDataBase64Encoding64CharacterLineLength];
}

@end
