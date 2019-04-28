#import <Flutter/Flutter.h>

typedef NS_ENUM(NSInteger, WDFNativeRouteEvent) {
    WDFNativeRouteEventOnCreate = 0,
    WDFNativeRouteEventOnPause,
    WDFNativeRouteEventOnResume,
    WDFNativeRouteEventBeforeDestroy,
    WDFNativeRouteEventOnDestroy
};

typedef NS_ENUM(NSInteger, WDFFlutterRouteEvent) {
    WDFFlutterRouteEventOnPush = 0,
    WDFFlutterRouteEventOnReplace,
    WDFFlutterRouteEventOnPop,
    WDFFlutterRouteEventOnRemove
};

@interface HybridRouterPlugin : NSObject<FlutterPlugin>

@property (nonatomic, strong) NSDictionary *mainEntryParams;

+ (instancetype)sharedInstance;

- (void)invokeFlutterMethod:(NSString *)method arguments:(id)arguments result:(void (^)(id result))callback;

- (void)invokeFlutterMethod:(NSString *)method arguments:(id)arguments;

@end
