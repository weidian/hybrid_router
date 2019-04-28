//
// Created by lm on 2018/11/15.
// Copyright (c) 2018 lm. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

@class UIViewController, WDFlutterViewWrapperController, WDFlutterRouteOptions;
typedef void(^FlutterToNativeCallback)(NSDictionary *dic);

@protocol WDFlutterURLRouterDelegate <NSObject>
    
@required

/**
 返回导航对象的回调，用于打开flutter页面

 @return 导航对象
 */
- (UIViewController *)flutterCurrentController;


/**
 flutter打开native页面的回调

 @param page native的页面名
 @param params 页面参数
 */
- (void)openNativePage:(NSString *)page params:(NSDictionary *)params;

@optional

/**
 获取flutter页面的容器，可以通过继承WDFlutterViewWrapperController来定制容器。默认使用WDFlutterViewWrapperController。

 @param routeOptions 路由参数
 @return flutter页面容器
 */
- (WDFlutterViewWrapperController *)flutterWrapperController:(WDFlutterRouteOptions *)routeOptions;

@end

@interface WDFlutterURLRouter : NSObject

@property (nonatomic, weak) id<WDFlutterURLRouterDelegate> delegate;

+ (instancetype)sharedInstance;

+ (void)openFlutterPage:(NSString *)page params:(NSDictionary *)params result:(FlutterResult)result;

+ (void)openNativePage:(NSString *)page params:(NSDictionary *)params;

+ (void)beforeNativePagePop:(NSString *)pageId result:(id)result;

+ (void)onNativePageRemoved:(NSString *)pageId result:(id)result;

+ (void)onNativePageReady:(NSString *)pageId;

+ (void)onFlutterPagePushed:(NSString *)pageId;

+ (void)onFlutterPageRemoved:(NSString *)pageId;

@end
