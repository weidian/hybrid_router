// MIT License
// -----------

// Copyright (c) 2019 WeiDian Group
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:

// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
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
