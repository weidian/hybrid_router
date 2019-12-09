//
//  WDFlutterRouteEventHandler.m
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import "WDFlutterRouteEventHandler.h"
#import "WDFlutterRouter.h"
#import "WDFlutterViewContainerManager.h"
#import "WDFlutterViewContainer+FlutterPage.h"

@implementation WDFlutterRouteEventHandler

+ (WDFlutterViewContainer *)find:(NSString *)pageId {
    WDFlutterViewContainer *vc = (WDFlutterViewContainer *)[WD_FLUTTER_ENGINE flutterViewController];
    return vc.options.nativePageId == pageId ? vc : nil;
}

#pragma mark -- container page

+ (void)beforeNativePagePop:(NSString *)pageId result:(id)result {

    WDFlutterViewContainer *controller = [self find:pageId];
    if(!controller) return;
    
    //防止页面回退 异常
    [controller surfaceUpdated:NO];
    [WD_FLUTTER_ENGINE detach];
    
    if ([controller respondsToSelector:@selector(nativePageWillRemove:)]) {
        [controller nativePageWillRemove:result];
    }
    
    if(controller.options.modal) {
        [controller dismissViewControllerAnimated:controller.options.animated completion:nil];
    } else {
        [[WDFlutterRouter.sharedInstance.delegate appNavigationController] popViewControllerAnimated:controller.options.animated];
    }
}

+ (void)onNativePageRemoved:(NSString *)pageId result:(id)result {
    WDFlutterViewContainer *controller = [self find:pageId];
    if (controller && [controller respondsToSelector:@selector(nativePageRemoved:)]) {
        [controller nativePageRemoved:result];
    }
}

+ (void)onNativePageResume:(NSString *)pageId {
    WDFlutterViewContainer *controller = [self find:pageId];
    if (controller && [controller respondsToSelector:@selector(nativePageResume)]) {
        [controller nativePageResume];
    }
}

#pragma mark -- flutter page

+ (void)onFlutterPagePushed:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *controller = [self find:pageId];
    if (controller && [controller respondsToSelector:@selector(flutterPagePushed:)]) {
        [controller flutterPagePushed:name];
    }
}

+ (void)onFlutterPageRemoved:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *controller = [self find:pageId];
    if (controller && [controller respondsToSelector:@selector(flutterPageRemoved:)]) {
        [controller flutterPageRemoved:name];
    }
}

+ (void)onFlutterPageResume:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *controller = [self find:pageId];
    if (controller && [controller respondsToSelector:@selector(flutterPageResume:)]) {
        [controller flutterPageResume:name];
    }
}

+ (void)onFlutterPagePause:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *controller = [self find:pageId];
    if (controller && [controller respondsToSelector:@selector(flutterPagePause:)]) {
        [controller flutterPagePause:name];
    }
}

@end
