//
// Created by lm on 2018/11/15.
// Copyright (c) 2018 lm. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "WDFlutterURLRouter.h"
#import "WDFlutterViewWrapperController.h"
#import "WDFlutterViewController.h"
#import "HybridRouterPlugin.h"

static long long fTag = 0;

@implementation WDFlutterURLRouter {
    bool _isFlutterWarmedup;
}
    
+ (instancetype)sharedInstance {
    static WDFlutterURLRouter *sInstance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sInstance = [WDFlutterURLRouter new];
    });
    return sInstance;
}

- (id)init {
    self = [super init];
    if (self) {
        [self warmupFlutter];
    }
    return self;
}

- (void)warmupFlutter {
    if (_isFlutterWarmedup) return;
    WDFlutterViewController *flutterVC = [WDFlutterViewWrapperController flutterVC];
    [flutterVC view];
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
    [NSClassFromString(@"GeneratedPluginRegistrant") performSelector:NSSelectorFromString(@"registerWithRegistry:") withObject:flutterVC];
#pragma clang diagnostic pop
    _isFlutterWarmedup = true;
}
    
- (UINavigationController *)getCurrentNavigationController {
    if ([[WDFlutterURLRouter sharedInstance].delegate respondsToSelector:@selector(flutterCurrentController)]) {
        UIViewController *vc= [[WDFlutterURLRouter sharedInstance].delegate flutterCurrentController];
        
        UINavigationController *nav;
        if ([vc isKindOfClass:[UINavigationController class]]) {
            nav = (UINavigationController *)vc;
        } else {
            nav = vc.navigationController;
        }
        return nav;
    }
    
    return nil;
}

- (void)openNativePage:(NSString *)page params:(NSDictionary *)paramsDic {
    if ([self.delegate respondsToSelector:@selector(openNativePage:params:)]) {
        [self.delegate openNativePage:page params:paramsDic];
    }
}

- (void)openFlutterPage:(NSString *)page params:(NSDictionary *)params result:(FlutterResult)result {
    WDFlutterRouteOptions *options = [WDFlutterRouteOptions new];
    options.pageName = page;
    options.nativePageId = [NSNumber numberWithLongLong:fTag].stringValue;
    options.args = params[@"args"];
    options.resultBlock = result;
    
    //Push
    WDFlutterViewWrapperController *viewController = nil;
    if ([self.delegate respondsToSelector:@selector(flutterWrapperController:)]) {
        id customController = [self.delegate flutterWrapperController:options];
        if ([customController isKindOfClass:[WDFlutterViewWrapperController class]]) {
            viewController = customController;
        }
    }
    
    if (!viewController) {
        viewController = [[WDFlutterViewWrapperController alloc] init];
    }
    
    if (!viewController.routeOptions) {
        viewController.routeOptions = options;
    }
    
    UINavigationController *nav = [self getCurrentNavigationController];
    if (!nav)   return;
    [nav pushViewController:viewController animated:YES];
    
    fTag++;
}

+ (void)openNativePage:(NSString *)page params:(NSDictionary *)params {
    [[WDFlutterURLRouter sharedInstance] openNativePage:page params:params];
}

+ (void)openFlutterPage:(NSString *)page params:(NSDictionary *)params result:(FlutterResult)result {
    [[WDFlutterURLRouter sharedInstance] openFlutterPage:page params:params result:result];
}

+ (void)beforeNativePagePop:(NSString *)pageId result:(id)result {
    UINavigationController *nav = [[WDFlutterURLRouter sharedInstance] getCurrentNavigationController];
    WDFlutterViewWrapperController *flutterVC = [self getFlutterController:pageId];
    if (!nav || !flutterVC) return;
    if (nav.topViewController == flutterVC) {
        [flutterVC onResult:result];
        [nav popViewControllerAnimated:YES];
    } else {
        [self removePage:pageId result:result];
    }
}

+ (void)onNativePageRemoved:(NSString *)pageId result:(id)result {
    [self removePage:pageId result:result];
}

+ (void)onNativePageReady:(NSString *)pageId {
    //do nothing
    //理论上需要根据这个回调，控制截屏消失的时间
}

+ (void)onFlutterPagePushed:(NSString *)pageId {
    WDFlutterViewWrapperController *flutterVC = [self getFlutterController:pageId];
    if (flutterVC) {
        [flutterVC flutterPagePushed];
    }
}

+ (void)onFlutterPageRemoved:(NSString *)pageId {
    WDFlutterViewWrapperController *flutterVC = [self getFlutterController:pageId];
    if (flutterVC) {
        [flutterVC flutterPageRemoved];
    }
}

#pragma mark - internal function
+ (void)removePage:(NSString *)pageId result:(id)result {
    WDFlutterViewWrapperController *controller = [self getFlutterController:pageId];
    if (controller) {
        [controller onResult:result];
        
        UINavigationController *nav = [[WDFlutterURLRouter sharedInstance] getCurrentNavigationController];
        if (!nav) return;
        NSMutableArray<UIViewController *> *viewControllers = nav.viewControllers.mutableCopy;
        [viewControllers removeObject:controller];
        nav.viewControllers = viewControllers.copy;
    }
}

+ (WDFlutterViewWrapperController *)getFlutterController:(NSString *)pageId {
    UINavigationController *nav = [[WDFlutterURLRouter sharedInstance] getCurrentNavigationController];
    if (!nav) return nil;
    NSMutableArray<UIViewController *> *viewControllers = nav.viewControllers.mutableCopy;
    for (NSInteger i = viewControllers.count - 1; i > 0; --i) {
        UIViewController *vc = viewControllers[i];
        if ([vc isKindOfClass:[WDFlutterViewWrapperController class]]) {
            WDFlutterViewWrapperController *flutterVC = (WDFlutterViewWrapperController *)vc;
            if ([flutterVC.routeOptions.nativePageId isEqualToString:pageId]) {
                return flutterVC;
            }
        }
    }
    
    return nil;
}
    
@end
