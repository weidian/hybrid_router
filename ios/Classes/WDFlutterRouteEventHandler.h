//
//  WDFlutterRouteEventHandler.h
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface WDFlutterRouteEventHandler : NSObject

#pragma mark -- container

+ (void)beforeNativePagePop:(NSString *)pageId result:(id)result;

+ (void)onNativePageRemoved:(NSString *)pageId result:(id)result;

+ (void)onNativePageResume:(NSString *)pageId;

#pragma mark -- flutter

+ (void)onFlutterPagePushed:(NSString *)pageId name:(NSString *)name;

+ (void)onFlutterPageRemoved:(NSString *)pageId name:(NSString *)name;

+ (void)onFlutterPageResume:(NSString *)pageId name:(NSString *)name;

+ (void)onFlutterPagePause:(NSString *)pageId name:(NSString *)name;

@end

NS_ASSUME_NONNULL_END
