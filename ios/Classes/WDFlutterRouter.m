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
//
// Created by lm on 2018/11/15.
// Copyright (c) 2018 lm. All rights reserved.
//

#import "WDFlutterRouter.h"
#import "WDFlutterViewContainer.h"
#import "WDFlutterViewContainerManager.h"

@interface WDFlutterRouter ()
@property(nonatomic, strong) WDFlutterViewContainerManager *manager;
@end

@implementation WDFlutterRouter

+ (instancetype)sharedInstance {
    static WDFlutterRouter *sInstance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sInstance = [WDFlutterRouter new];
    });
    return sInstance;
}

- (id)init {
    self = [super init];
    if (self) {
        _manager = [WDFlutterViewContainerManager new];
    }
    return self;
}

- (void)openNativePage:(NSString *)page params:(id)paramsDic transitionType:(WDFlutterRouterTransitionType)type {
    if ([self.delegate respondsToSelector:@selector(openNativePage:params:transitionType:)]) {
        [self.delegate openNativePage:page params:paramsDic transitionType:type];
    }
}

- (void)openFlutterPage:(NSString *)page
                 params:(id)params
                 result:(FlutterResult)result {

    [self openFlutterPage:page
                   params:params
           transitionType:WDFlutterRouterTransitionTypeDefault
                   result:result];
}

- (void)openFlutterPage:(NSString *)page
                 params:(id)params
         transitionType:(WDFlutterRouterTransitionType)type
                 result:(FlutterResult)result {

    [self openFlutterPage:page
                   params:params
                   result:result
                    modal:type == WDFlutterRouterTransitionTypeBottomToTop
                 animated:YES];
}

- (void)openFlutterPage:(NSString *)page
                 params:(id)params
                 result:(FlutterResult)result
                  modal:(BOOL)modal
               animated:(BOOL)animated {

    WDFlutterRouteOptions *options = [WDFlutterRouteOptions new];
    options.pageName = page;
    options.args = params;
    options.resultBlock = result;
    options.modal = modal;
    options.animated = animated;

    WDFlutterViewContainer *viewController = nil;
    if ([_delegate respondsToSelector:@selector(flutterViewContainer)]) {
        viewController = [_delegate flutterViewContainer];
    }
    if (viewController == nil) {
        viewController = [[WDFlutterViewContainer alloc] init];
    }
    viewController.routeOptions = options;

    UINavigationController *nav = _delegate.appNavigationController;
    if (!nav) return;

    if (!modal) {
        [nav pushViewController:viewController animated:animated];
    } else {
        UIViewController *_viewController = [[UINavigationController alloc] initWithRootViewController:viewController];
        _viewController.modalPresentationStyle = UIModalPresentationFullScreen;
        [nav presentViewController:_viewController
                          animated:animated
                        completion:nil];
    }
}

#pragma mark -- container

- (void)add:(WDFlutterViewContainer *)container {
    [_manager add:container];
}

- (void)remove:(WDFlutterViewContainer *)container {
    [_manager remove:container];
}

#pragma mark -- manager

- (WDFlutterViewContainerManager *)contaninerManger {
    return _manager;
}

@end
