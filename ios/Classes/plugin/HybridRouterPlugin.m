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
@property(nonatomic,strong) NSMutableDictionary *popResult;

@property(nonatomic,strong) NSMutableArray *popedIds;

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
        sharedInst.popResult = [NSMutableDictionary dictionary];
        sharedInst.popedIds = [NSMutableArray array];
    });
    return sharedInst;
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    NSString *method = call.method;
    if ([@"getInitRoute" isEqualToString:method]) {
        NSDictionary *params = self.mainEntryParams ?: @{};
        result(params);
    } else if ([@"openNativePage" isEqualToString:method]) {
        [self openNativePage:call.arguments result:result];
        result(nil);
    } else if ([@"openFlutterPage" isEqualToString:method]) {
        [self openFlutterPage:call.arguments result:result];
    } else if ([@"onNativeRouteEvent" isEqualToString:method]) {
        [self onNativeRouteEvent:call.arguments result:result];
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
            if([_popedIds containsObject:nativePageId]) {
                result(nil);
                [_popedIds removeObject:nativePageId];
                break;
            }
            
            _popResult[nativePageId] = result;
            [WDFlutterRouteEventHandler beforeNativePagePop:nativePageId result:_result];
            break;
        case WDFNativeRouteEventOnDestroy:
            [WDFlutterRouteEventHandler onNativePageRemoved:nativePageId result:_result];
            break;
        case WDFNativeRouteEventOnResume:
            [WDFlutterRouteEventHandler onNativePageResume:nativePageId];
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
    [WDFlutterRouter.sharedInstance openFlutterPage:arguments[@"pageName"] params:arguments[@"args"] result:result];
}

#pragma mark - open native page

- (void)openNativePage:(NSDictionary *)arguments result:(FlutterResult)result {
    [WDFlutterRouter.sharedInstance openNativePage:arguments[@"url"] params:arguments[@"args"] transitionType:[arguments[@"transitionType"] intValue]];
}

#pragma mark - invoke method

- (void)invokeFlutterMethod:(NSString *)method arguments:(id)arguments result:(void (^)(id result))callback {
    [self.methodChannel invokeMethod:method arguments:arguments result:callback];
}

- (void)invokeFlutterMethod:(NSString *)method arguments:(id)arguments {
    [self.methodChannel invokeMethod:method arguments:arguments];
}

- (void)popDone:(NSString *)nativePageId {
    FlutterResult result = _popResult[nativePageId];
    if(result) {
        result(nil);
        [_popResult removeObjectForKey:nativePageId];
    } else {
        [_popedIds addObject:nativePageId];
    }
}

@end
