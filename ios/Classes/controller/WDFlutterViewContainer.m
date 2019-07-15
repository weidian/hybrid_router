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

#import "WDFlutterViewContainer.h"
#import "HybridRouterPlugin.h"
#import "WDFlutterRouter.h"
#import "WDFlutterEngine.h"

#define FLUTTER_VIEWCONTROLLER WDFlutterEngine.sharedInstance.viewController
#define FLUTTER_VIEWCONTROLLER_VIEW WDFlutterEngine.sharedInstance.viewController.view

@interface WDFlutterViewContainer ()
@property(nonatomic, strong) UIImageView *fakeSnapImgView;
@property(nonatomic, strong) UIImage *lastSnapshot;
@end

@implementation WDFlutterViewContainer {
    BOOL _isFirstOpen;
    int _flutterPageCount;
    long long _pageId;
    BOOL _changeTab;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        static long long fTag = 0;
        _pageId = fTag++;
        _isFirstOpen = YES;
        _flutterPageCount = 0;
        self.hidesBottomBarWhenPushed = YES;
    }
    return self;
}

- (void)loadView {
    UIView *view = [[UIView alloc] init];
    [view setBackgroundColor:[UIColor whiteColor]];
    self.view = view;
}

- (BOOL)shouldAutomaticallyForwardAppearanceMethods {
    return true;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.fakeSnapImgView = [[UIImageView alloc] initWithFrame:self.view.bounds];
    self.fakeSnapImgView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    [self.fakeSnapImgView setBackgroundColor:[UIColor whiteColor]];
    [self.view addSubview:self.fakeSnapImgView];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    if ([[FLUTTER_VIEWCONTROLLER parentViewController] isEqual:self]) {
        [FLUTTER_VIEWCONTROLLER didReceiveMemoryWarning];
    }
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.navigationController.navigationBar.hidden = YES;

    static BOOL sIsFirstPush = YES;

    if (_isFirstOpen) {
        _routeOptions.nativePageId = @(_pageId).stringValue;
        [WDFlutterRouter.sharedInstance add:self];
        if (sIsFirstPush) {
            [HybridRouterPlugin sharedInstance].mainEntryParams = [_routeOptions toDictionary];
            sIsFirstPush = NO;
        } else {
            [[HybridRouterPlugin sharedInstance] invokeFlutterMethod:@"pushFlutterPage" arguments:[_routeOptions toDictionary]];
        }
        _isFirstOpen = NO;

        dispatch_async(dispatch_get_main_queue(), ^{
            [self addChildFlutterVC];
        });
    } else {
        if (_changeTab) {
            [[HybridRouterPlugin sharedInstance] invokeFlutterMethod:@"onNativePageResumed" arguments:@{@"nativePageId": self.routeOptions.nativePageId}];
        }
    }
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    if (_lastSnapshot) {
        [self nativePageResume];
    }
    [FLUTTER_VIEWCONTROLLER_VIEW setUserInteractionEnabled:TRUE];
    //只能在didAppear里调用，willAppear里调用会导致导航栈bug
    self.navigationController.interactivePopGestureRecognizer.enabled = (_flutterPageCount <= 1);
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self saveSnapshot];
    _changeTab = false;
    UIViewController *topViewController = self.navigationController.viewControllers.lastObject;
    if (topViewController == self) {
        _changeTab = YES;
    } else {
        if (![topViewController isKindOfClass:self.class] && topViewController.childViewControllers) {
            _changeTab = topViewController.childViewControllers.lastObject == self;
        }
    }

    [FLUTTER_VIEWCONTROLLER_VIEW setUserInteractionEnabled:FALSE];
}

- (void)flutterPagePushed:(NSString *)pageName {
    _flutterPageCount++;
    if (_flutterPageCount > 1) {
        self.navigationController.interactivePopGestureRecognizer.enabled = NO;
    } else {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t) (0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self.view bringSubviewToFront:FLUTTER_VIEWCONTROLLER_VIEW];
            if (self.navigationController.topViewController == self) {
                self.lastSnapshot = nil;
            }
        });
    }
}

- (void)flutterPageRemoved:(NSString *)pageName {
    _flutterPageCount--;
    if (_flutterPageCount <= 1) {
        self.navigationController.interactivePopGestureRecognizer.enabled = YES;
    }
}

- (void)nativePageWillRemove:(id)result {
    if (self.routeOptions.resultBlock) {
        if (result) {
            self.routeOptions.resultBlock(@{@"data": result});
        } else {
            self.routeOptions.resultBlock(@{});
        }
    }

    [self saveSnapshot];
    [WDFlutterRouter.sharedInstance remove:self];
}

- (void)nativePageResume {
    UIViewController *topViewController  = self.navigationController.topViewController;
    if (topViewController != self.parentViewController && topViewController != self) {
        return;
    }

    [self addChildFlutterVC];

    if (_lastSnapshot) {
        [self.view bringSubviewToFront:self.fakeSnapImgView];
    }

    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t) (0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.view bringSubviewToFront:FLUTTER_VIEWCONTROLLER_VIEW];
        self.lastSnapshot = nil;
    });
}

#pragma mark - Child/Parent VC

- (void)addChildFlutterVC {
    if (self == FLUTTER_VIEWCONTROLLER.parentViewController) {
        return;
    }
    if (nil != FLUTTER_VIEWCONTROLLER.parentViewController) {
        [self removeChildFlutterVC];
    }
    FLUTTER_VIEWCONTROLLER_VIEW.frame = self.view.bounds;
    FLUTTER_VIEWCONTROLLER_VIEW.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

    [self.view addSubview:FLUTTER_VIEWCONTROLLER_VIEW];
    [self addChildViewController:FLUTTER_VIEWCONTROLLER];

    if (!_lastSnapshot) {
        [self.view bringSubviewToFront:self.fakeSnapImgView];
    }
}

- (void)removeChildFlutterVC {
    [FLUTTER_VIEWCONTROLLER removeFromParentViewController];
    [FLUTTER_VIEWCONTROLLER_VIEW removeFromSuperview];
}

- (void)saveSnapshot {
    if (FLUTTER_VIEWCONTROLLER.parentViewController != self) {
        return;
    }
    if (self.lastSnapshot == nil) {
        UIGraphicsBeginImageContextWithOptions(self.view.bounds.size, YES, 0);
        [FLUTTER_VIEWCONTROLLER_VIEW drawViewHierarchyInRect:FLUTTER_VIEWCONTROLLER_VIEW.bounds afterScreenUpdates:NO];
        self.lastSnapshot = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        [self.fakeSnapImgView setImage:self.lastSnapshot];
        [self.view bringSubviewToFront:self.fakeSnapImgView];
    }
}

- (void)didMoveToParentViewController:(UIViewController *)parent {
    [super didMoveToParentViewController:parent];
    if (parent == nil) {
        //当前controllere被remove
        [[HybridRouterPlugin sharedInstance] invokeFlutterMethod:@"onNativePageFinished" arguments:@{@"nativePageId": self.routeOptions.nativePageId}];
    }
}

@end

@implementation WDFlutterRouteOptions

- (NSDictionary *)toDictionary {
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    if (_args) {
        dictionary[@"args"] = _args;
    }
    dictionary[@"pageName"] = _pageName ?: @"";
    dictionary[@"nativePageId"] = _nativePageId ?: @"";
    dictionary[@"isTab"] = @(_isTab);
    return dictionary;
}

@end
