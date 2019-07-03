//
//  WDFlutterRouteEventHandler.m
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import "WDFlutterRouteEventHandler.h"
#import "WDFlutterRouter.h"
#import "WDFlutterViewContainerManager.h"
#import "WDFlutterViewContainer.h"

#define FLUTTER_CONTAINER_MANAGER

@implementation WDFlutterRouteEventHandler

+ (WDFlutterViewContainer *)find:(NSString *)pageId {
    WDFlutterViewContainerManager *manager = [WDFlutterRouter.sharedInstance contaninerManger];
    return [manager find:pageId];
}

#pragma mark -- container page

+ (void)beforeNativePagePop:(NSString *)pageId result:(id)result {
    WDFlutterViewContainer *container = [self find:pageId];
    UINavigationController *nav = container.navigationController;
    
    if (!nav || !container) return;
    
    if (nav.topViewController == container) {
        [container nativePageWillRemove:result];
        [container.navigationController popViewControllerAnimated:YES];
    } else {
        [self removeContainer:container];
    }
}

+ (void)onNativePageRemoved:(NSString *)pageId result:(id)result {
    WDFlutterViewContainer *container = [self find:pageId];
    if (container) {
        [container nativePageWillRemove:result];
        [self removeContainer:container];
    }
}

+ (void)onNativePageResume:(NSString *)pageId {
    WDFlutterViewContainer *container = [self find:pageId];
    if (container) {
        [container nativePageResume];
    }
}

+ (void)removeContainer:(WDFlutterViewContainer *)container {
    UINavigationController *nav = container.navigationController;
    if (!nav) return;
    NSMutableArray<UIViewController *> *viewControllers = nav.viewControllers.mutableCopy;
    [viewControllers removeObject:container];
    nav.viewControllers = viewControllers.copy;
}

#pragma mark -- flutter page

+ (void)onFlutterPagePushed:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *container = [self find:pageId];
    if (container && [container respondsToSelector:@selector(flutterPagePushed:)]) {
        [container flutterPagePushed:name];
    }
}

+ (void)onFlutterPageRemoved:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *container = [self find:pageId];
    if (container && [container respondsToSelector:@selector(flutterPageRemoved:)]) {
        [container flutterPageRemoved:name];
    }
}

+ (void)onFlutterPageResume:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *container = [self find:pageId];
    if (container && [container respondsToSelector:@selector(flutterPageResume:)]) {
        [container flutterPageResume:name];
    }
}

+ (void)onFlutterPagePause:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *container = [self find:pageId];
    if (container && [container respondsToSelector:@selector(flutterPagePause:)]) {
        [container flutterPagePause:name];
    }
}

@end
