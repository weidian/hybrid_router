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

#pragma mark -- fluttr page

+ (void)onFlutterPagePushed:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *container = [FLUTTER_CONTAINER_MANAGER find:pageId];
    if (container) {
        [container flutterPagePushed];
        if([FLUTTER_ROUTER_DELEGATE respondsToSelector:@selector(flutterViewDidAppear:name:)]) {
            [FLUTTER_ROUTER_DELEGATE flutterViewDidAppear:container name:name];
        }
    }
}

+ (void)onFlutterPageRemoved:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *container = [FLUTTER_CONTAINER_MANAGER find:pageId];
    if (container) {
        [container flutterPageRemoved];
        if([FLUTTER_ROUTER_DELEGATE respondsToSelector:@selector(flutterViewDidRemove:name:)]) {
            [FLUTTER_ROUTER_DELEGATE flutterViewDidRemove:container name:name];
        }
    }
}

+ (void)onFlutterPageResume:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *container = [FLUTTER_CONTAINER_MANAGER find:pageId];
    if (container) {
        if([FLUTTER_ROUTER_DELEGATE respondsToSelector:@selector(flutterViewDidAppear:name:)]) {
            [FLUTTER_ROUTER_DELEGATE flutterViewDidAppear:container name:name];
        }
    }
}

+ (void)onFlutterPagePause:(NSString *)pageId name:(NSString *)name {
    WDFlutterViewContainer *container = [FLUTTER_CONTAINER_MANAGER find:pageId];
    if (container) {
        if([FLUTTER_ROUTER_DELEGATE respondsToSelector:@selector(flutterViewDidDisappear:name:)]) {
            [FLUTTER_ROUTER_DELEGATE flutterViewDidDisappear:container name:name];
        }
    }
}

+ (void)onFlutterViewRender:(NSString *)pageId time:(CFAbsoluteTime)time {
    WDFlutterViewContainer *container = [FLUTTER_CONTAINER_MANAGER find:pageId];
    if (container) {
        if ([FLUTTER_ROUTER_DELEGATE respondsToSelector:@selector(flutterViewDidRender:time:)]) {
            [FLUTTER_ROUTER_DELEGATE flutterViewDidRender:container.routeOptions.pageName time:time];
        }
    }
}

@end
