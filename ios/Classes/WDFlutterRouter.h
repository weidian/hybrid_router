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

@class WDFlutterViewContainer, WDFlutterViewContainerManager,WDFlutterViewController;
@class UIViewController, WDFlutterViewContainer, WDFlutterRouteOptions;

typedef NS_ENUM(int, WDFlutterRouterTransitionType) {
    WDFlutterRouterTransitionTypeDefault = 0,
    WDFlutterRouterTransitionTypeBottomToTop = 1,
    WDFlutterRouterTransitionTypeRightToLeft = 2,
};

@protocol WDFlutterRouterDelegate <NSObject>

@required
/**
 flutter打开native页面的回调

 @param page native的页面名
 @param params 页面参数
 */
- (void)openNativePage:(NSString *)page params:(id)params transitionType:(WDFlutterRouterTransitionType)type;

- (UINavigationController *)appNavigationController;

@optional

/**
 获取flutter页面的容器，可以通过继承WDFlutterViewContainer来定制容器。默认使用WDFlutterViewContainer。
 @return flutter页面容器
 */
- (WDFlutterViewContainer *)flutterViewContainer;

@end

@interface WDFlutterRouter : NSObject

@property(nonatomic, weak) id <WDFlutterRouterDelegate> delegate;

+ (instancetype)sharedInstance;

- (void)openFlutterPage:(NSString *)page params:(id)params result:(FlutterResult)result;

- (void)openFlutterPage:(NSString *)page
                 params:(id)params
                 result:(FlutterResult)result
                  modal:(BOOL)modal
               animated:(BOOL)animated;

- (void)openNativePage:(NSString *)page params:(id)params transitionType:(WDFlutterRouterTransitionType)type;

#pragma mark -- container

- (void)add:(WDFlutterViewController *)container;

- (void)remove:(WDFlutterViewController *)container;

#pragma mark -- manager

- (WDFlutterViewContainerManager *)contaninerManger;

@end
