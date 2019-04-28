#import "HybridRouterPlugin.h"
#import "WDFlutterURLRouter.h"

@interface HybridRouterPlugin()
@property (nonatomic, strong) FlutterMethodChannel *methodChannel;
@end

@implementation HybridRouterPlugin

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"com.vdian.flutter.hybridrouter"
            binaryMessenger:[registrar messenger]];
    HybridRouterPlugin* instance = [HybridRouterPlugin sharedInstance];
    instance.methodChannel = channel;
    [registrar addMethodCallDelegate:instance channel:channel];
}

+ (instancetype)sharedInstance{
    static HybridRouterPlugin * sharedInst;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInst = [[HybridRouterPlugin alloc] init];
    });
    return sharedInst;
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSString *method = call.method;
    if ([@"getInitRoute" isEqualToString:method]) {
        NSDictionary *params = self.mainEntryParams?:@{};
        result(params);
    } else if ([@"openNativePage" isEqualToString:method]) {
        [self openNativePage:call.arguments result:result];
        result(nil);
    } else if ([@"openFlutterPage" isEqualToString:method]) {
        [self openFlutterPage:call.arguments result:result];
    } else if ([@"onNativeRouteEvent" isEqualToString:method]) {
        [self onNativeRouteEvent:call.arguments];
        result(nil);
    } else if ([@"onFlutterRouteEvent" isEqualToString:method]) {
        [self onFlutterRouteEvent:call.arguments];
        result(nil);
    } else {
        result(FlutterMethodNotImplemented);
    }
}

#pragma mark - native route event
- (void)onNativeRouteEvent:(NSDictionary *)arguments {
    NSString *nativePageId = arguments[@"nativePageId"];
    NSNumber *eventId = arguments[@"eventId"];
    id result = arguments[@"result"];
    switch (eventId.integerValue) {
        case WDFNativeRouteEventBeforeDestroy:
            [WDFlutterURLRouter beforeNativePagePop:nativePageId result:result];
            break;
        case WDFNativeRouteEventOnDestroy:
            [WDFlutterURLRouter onNativePageRemoved:nativePageId result:result];
            break;
        case WDFNativeRouteEventOnResume:
            [WDFlutterURLRouter onNativePageReady:nativePageId];
            break;
        default:
            break;
    }
}

#pragma mark - flutter route event
- (void)onFlutterRouteEvent:(NSDictionary *)arguments {
    NSString *nativePageId = arguments[@"nativePageId"];
    NSNumber *eventId = arguments[@"eventId"];
    switch (eventId.integerValue) {
        case WDFFlutterRouteEventOnPush:
            [WDFlutterURLRouter onFlutterPagePushed:nativePageId];
            break;
        case WDFFlutterRouteEventOnReplace:
            //do nothing
            break;
        case WDFFlutterRouteEventOnPop:
            [WDFlutterURLRouter onFlutterPageRemoved:nativePageId];
            break;
        case WDFFlutterRouteEventOnRemove:
            [WDFlutterURLRouter onFlutterPageRemoved:nativePageId];
            break;
        default:
            break;
    }
}

#pragma mark - open flutter page
- (void)openFlutterPage:(NSDictionary *)arguments result:(FlutterResult)result {
    [WDFlutterURLRouter openFlutterPage:arguments[@"pageName"] params:arguments result:result];
}

#pragma mark - open native page
- (void)openNativePage:(NSDictionary *)arguments result:(FlutterResult)result {
    [WDFlutterURLRouter openNativePage:arguments[@"url"] params:arguments[@"args"]];
}

#pragma mark - invoke method
- (void)invokeFlutterMethod:(NSString *)method arguments:(id)arguments result:(void (^)(id result))callback {
    [self.methodChannel invokeMethod:method arguments:arguments result:callback];
}

- (void)invokeFlutterMethod:(NSString *)method arguments:(id)arguments {
    [self.methodChannel invokeMethod:method arguments:arguments];
}

@end
