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

@implementation WDFlutterRouteEventHandler

+ (WDFlutterViewController *)find:(NSString *)pageId {
    WDFlutterViewContainerManager *manager = [WDFlutterRouter.sharedInstance contaninerManger];
    return [manager find:pageId];
}

#pragma mark -- container page

+ (void)beforeNativePagePop:(NSString *)pageId result:(id)result {

    WDFlutterViewController *container = [self find:pageId];
    if(!container) return;
    [[WDFlutterRouter.sharedInstance.delegate appNavigationController] popViewControllerAnimated:YES];
    
    /*WDFlutterViewContainer *container = [self find:pageId];
    static int _count = 0;

    if (!container || !container.didAppear) {
        _count++;
        if (_count > 10) {
            return;
        }
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t) (0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [WDFlutterRouteEventHandler beforeNativePagePop:pageId result:result];
        });
        return;
    }

    _count = 0;

    [container nativePageWillRemove:result];

    if (!container.routeOptions.modal) {
        UINavigationController *nav = container.navigationController;
        if (nav.topViewController == container) {
            [container.navigationController popViewControllerAnimated:container.routeOptions.animated];
        } else {
            [self removeContainer:container];
        }
    } else {
        [container dismissViewControllerAnimated:container.routeOptions.animated completion:nil];
    }*/
}

+ (void)onNativePageRemoved:(NSString *)pageId result:(id)result {
//    WDFlutterViewContainer *container = [self find:pageId];
//    if (container) {
//        //[container nativePageWillRemove:result];
//        //[self removeContainer:container];
//    }
}

+ (void)onNativePageResume:(NSString *)pageId {
//    WDFlutterViewContainer *container = [self find:pageId];
//    if (container) {
//        [container nativePageResume];
//    }
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
//    WDFlutterViewContainer *container = [self find:pageId];
//    if (container && [container respondsToSelector:@selector(flutterPagePushed:)]) {
//        [container flutterPagePushed:name];
//    }
}

+ (void)onFlutterPageRemoved:(NSString *)pageId name:(NSString *)name {
//    WDFlutterViewContainer *container = [self find:pageId];
//    if (container && [container respondsToSelector:@selector(flutterPageRemoved:)]) {
//        [container flutterPageRemoved:name];
//    }
}

+ (void)onFlutterPageResume:(NSString *)pageId name:(NSString *)name {
//    WDFlutterViewContainer *container = [self find:pageId];
//    if (container && [container respondsToSelector:@selector(flutterPageResume:)]) {
//        [container flutterPageResume:name];
//    }
}

+ (void)onFlutterPagePause:(NSString *)pageId name:(NSString *)name {
//    WDFlutterViewContainer *container = [self find:pageId];
//    if (container && [container respondsToSelector:@selector(flutterPagePause:)]) {
//        [container flutterPagePause:name];
//    }
}

@end
