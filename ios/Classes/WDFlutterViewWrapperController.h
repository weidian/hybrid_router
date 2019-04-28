//
// Created by lm on 2018/11/15.
// Copyright (c) 2018 lm. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <Flutter/Flutter.h>

@class WDFlutterViewController, WDFlutterRouteOptions;

@interface WDFlutterViewWrapperController : UIViewController

+ (WDFlutterViewController *)flutterVC;

@property (nonatomic, strong) WDFlutterRouteOptions *routeOptions;

- (void)onResult:(id)result;

- (void)flutterPagePushed;

- (void)flutterPageRemoved;

@end

@interface WDFlutterRouteOptions : NSObject

@property (nonatomic, copy) NSString *nativePageId;

@property (nonatomic, strong) NSDictionary *args;

@property (nonatomic, copy) NSString *pageName;

@property (nonatomic, strong) FlutterResult resultBlock;

- (NSDictionary *)toDictionary;

@end
