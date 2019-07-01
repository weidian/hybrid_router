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

#define FLUTTER_CONTAINER_MANAGER [WDFlutterRouter.sharedInstance contaninerManger]
#define FLUTTER_ROUTER_DELEGATE [WDFlutterRouter.sharedInstance delegate]

@implementation WDFlutterRouteEventHandler

#pragma mark -- container page

+ (void)beforeNativePagePop:(NSString *)pageId result:(id)result {
    WDFlutterViewContainer *container = [FLUTTER_CONTAINER_MANAGER find:pageId];
    if (container) {
        [container nativePageWillRemove:result];
        [container.navigationController popViewControllerAnimated:YES];
    }
}

+ (void)onNativePageRemoved:(NSString *)pageId result:(id)result {
    WDFlutterViewContainer *container = [FLUTTER_CONTAINER_MANAGER find:pageId];
    if (container) {
        [container nativePageWillRemove:result];
    }
}

+ (void)onNativePageResume:(NSString *)pageId {
    WDFlutterViewContainer *container = [FLUTTER_CONTAINER_MANAGER find:pageId];
    if (container) {
        [container nativePageResume];
    }
}

#pragma mark -- flutter page

+ (void)onFlutterPagePushed:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *container = [FLUTTER_CONTAINER_MANAGER find:pageId];
    if (container && [container respondsToSelector:@selector(flutterPagePushed)]) {
        [container flutterPagePushed];
    }
}

+ (void)onFlutterPageRemoved:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *container = [FLUTTER_CONTAINER_MANAGER find:pageId];
    if (container && [container respondsToSelector:@selector(flutterPageRemoved)]) {
        [container flutterPageRemoved];
    }
}

+ (void)onFlutterPageResume:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *container = [FLUTTER_CONTAINER_MANAGER find:pageId];
    if (container && [container respondsToSelector:@selector(flutterPageResume)]) {
        [container flutterPageResume];
    }
}

+ (void)onFlutterPagePause:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *container = [FLUTTER_CONTAINER_MANAGER find:pageId];
    if (container && [container respondsToSelector:@selector(flutterPagePause)]) {
        [container flutterPagePause];
    }
}

@end
