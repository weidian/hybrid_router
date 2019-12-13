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
#import "HybridRouterPlugin.h"
#import "WDFlutterRouter.h"
#import "WDFlutterRouteEventHandler.h"

@interface HybridRouterPlugin ()
@property(nonatomic, strong) FlutterMethodChannel *methodChannel;

@property(nonatomic, assign) BOOL initialized;
@property(nonatomic, copy) NSString *method;
@property(nonatomic, strong) id arguments;

@end

@implementation HybridRouterPlugin

+ (void)registerWithRegistrar:(NSObject <FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel *channel = [FlutterMethodChannel
            methodChannelWithName:@"com.vdian.flutter.hybridrouter"
                  binaryMessenger:[registrar messenger]];
    HybridRouterPlugin *instance = [HybridRouterPlugin sharedInstance];
    instance.methodChannel = channel;
    [registrar addMethodCallDelegate:instance channel:channel];
}

+ (instancetype)sharedInstance {
    static HybridRouterPlugin *sharedInst;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInst = [[HybridRouterPlugin alloc] init];
    });
    return sharedInst;
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    NSString *method = call.method;
    if ([@"getInitRoute" isEqualToString:method]) {
        NSDictionary *params = self.mainEntryParams ?: @{};
        result(params);
        _initialized = YES;
        if(_method) {
            [self.methodChannel invokeMethod:_method arguments:_arguments];
        }
    } else if ([@"openNativePage" isEqualToString:method]) {
        [self openNativePage:call.arguments result:result];
    } else if ([@"openFlutterPage" isEqualToString:method]) {
        [self openFlutterPage:call.arguments result:result];
    } else if ([@"onNativeRouteEvent" isEqualToString:method]) {
        [self onNativeRouteEvent:call.arguments result:result];
        result(nil);
    } else if ([@"onFlutterRouteEvent" isEqualToString:method]) {
        [self onFlutterRouteEvent:call.arguments];
        result(nil);
    } else {
        result(FlutterMethodNotImplemented);
    }
}

#pragma mark - native route event

- (void)onNativeRouteEvent:(NSDictionary *)arguments result:(FlutterResult)result {
    NSString *nativePageId = arguments[@"nativePageId"];
    NSNumber *eventId = arguments[@"eventId"];
    id _result = arguments[@"result"];
    switch (eventId.integerValue) {
        case WDFNativeRouteEventBeforeDestroy:
            [WDFlutterRouteEventHandler beforeNativePagePop:nativePageId result:_result];
            break;
        case WDFNativeRouteEventOnDestroy:
            [WDFlutterRouteEventHandler onNativePageRemoved:nativePageId result:_result];
            break;
        case WDFNativeRouteEventOnResume:
            [WDFlutterRouteEventHandler onNativePageResume:nativePageId];
            break;
        case WDFNativeRouteEventOnCreate:
            [WDFlutterRouteEventHandler onNativePageCreate:nativePageId];
            break;
        default:
            break;
    }
}

#pragma mark - flutter route event

- (void)onFlutterRouteEvent:(NSDictionary *)arguments {
    NSString *nativePageId = arguments[@"nativePageId"];
    NSNumber *eventId = arguments[@"eventId"];
    NSString *name = arguments[@"name"];
    switch (eventId.integerValue) {
        case WDFFlutterRouteEventOnPush:
            [WDFlutterRouteEventHandler onFlutterPagePushed:nativePageId name:name];
            break;
        case WDFFlutterRouteEventOnResume:
            [WDFlutterRouteEventHandler onFlutterPageResume:nativePageId name:name];
            break;
        case WDFFlutterRouteEventOnPause:
            [WDFlutterRouteEventHandler onFlutterPagePause:nativePageId name:name];
            break;
        case WDFFlutterRouteEventOnReplace:
            //do nothing
            break;
        case WDFFlutterRouteEventOnPop:
            [WDFlutterRouteEventHandler onFlutterPageRemoved:nativePageId name:name];
            break;
        case WDFFlutterRouteEventOnRemove:
            [WDFlutterRouteEventHandler onFlutterPageRemoved:nativePageId name:name];
            break;
        default:
            break;
    }
}

#pragma mark - open flutter page

- (void)openFlutterPage:(NSDictionary *)arguments result:(FlutterResult)result {
    [WDFlutterRouter.sharedInstance openFlutterPage:arguments[@"pageName"]
                                             params:arguments[@"args"]
                                     transitionType:(WDFlutterRouterTransitionType) [arguments[@"transitionType"] intValue]
                                             result:result];
}

#pragma mark - open native page

- (void)openNativePage:(NSDictionary *)arguments result:(FlutterResult)result {
    [WDFlutterRouter.sharedInstance openNativePage:arguments[@"url"]
                                            params:arguments[@"args"]
                                    transitionType:(WDFlutterRouterTransitionType) [arguments[@"transitionType"] intValue]];
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        result(nil);
    });
}

#pragma mark - invoke method

- (void)invokeFlutterMethod:(NSString *)method arguments:(id)arguments result:(void (^)(id result))callback {
    [self.methodChannel invokeMethod:method arguments:arguments result:callback];
}

- (void)invokeFlutterMethod:(NSString *)method arguments:(id)arguments {
    if(_initialized) {
        [self.methodChannel invokeMethod:method arguments:arguments];
    } else {
        _method = method;
        _arguments = arguments;
    }
}

@end
