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

#import "WDFlutterViewWrapperController.h"
#import "WDFlutterViewController.h"
#import "HybridRouterPlugin.h"
#import "WDFlutterURLRouter.h"

@interface WDFlutterViewWrapperController ()

@property (nonatomic, strong) UIImageView *fakeSnapImgView;
@property (nonatomic, strong) UIImage *lastSnapshot;
@property (nonatomic, strong) UINavigationController *currentNav;

@end

@implementation WDFlutterViewWrapperController {
    BOOL _isFirstOpen;
    int _flutterPageCount;
    long long _pageId;
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
    [self.fakeSnapImgView setBackgroundColor:[UIColor clearColor]];
    [self.view addSubview:self.fakeSnapImgView];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    WDFlutterViewController *flutterVC = [WDFlutterViewWrapperController flutterVC];
    if ([[flutterVC parentViewController] isEqual:self]) {
        [flutterVC didReceiveMemoryWarning];
    }
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.currentNav = self.navigationController;
    self.navigationController.navigationBar.hidden = YES;
    
    static BOOL sIsFirstPush = YES;
    
    if (_isFirstOpen) {
        _routeOptions.nativePageId = @(_pageId).stringValue;
        if(sIsFirstPush) {
            [HybridRouterPlugin sharedInstance].mainEntryParams = [_routeOptions toDictionary]; //{"pageName": 路由地址, "args": {}}
            sIsFirstPush = NO;
        } else {
            [[HybridRouterPlugin sharedInstance] invokeFlutterMethod:@"pushFlutterPage" arguments:[_routeOptions toDictionary] result:^(id result) {
            }];
        }
        _isFirstOpen = NO;
    }
    
    if (!self.lastSnapshot) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self addChildFlutterVC];
        });
    }
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [self addChildFlutterVC];
    [[WDFlutterViewWrapperController flutterVC].view setUserInteractionEnabled:TRUE];
    
    //只能在didAppear里调用，willAppear里调用会导致导航栈bug
    self.navigationController.interactivePopGestureRecognizer.enabled = (_flutterPageCount <= 1);
}

- (void)viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    NSArray *curStackAry = self.currentNav.viewControllers;
    NSInteger idx = [curStackAry indexOfObject:self];
    if(idx != NSNotFound && idx != curStackAry.count-1){
        [self saveSnapshot];
    }
    [[WDFlutterViewWrapperController flutterVC].view setUserInteractionEnabled:FALSE];
}

- (void)flutterPagePushed {
    _flutterPageCount ++;
    if (_flutterPageCount > 1) {
        self.navigationController.interactivePopGestureRecognizer.enabled = NO;
    }
}

- (void)flutterPageRemoved {
    _flutterPageCount --;
    if (_flutterPageCount <= 1) {
        self.navigationController.interactivePopGestureRecognizer.enabled = YES;
    }
}

- (void)onResult:(id)result {
    if (self.routeOptions.resultBlock) {
        if (result) {
            self.routeOptions.resultBlock(@{@"data" : result});
        } else {
            self.routeOptions.resultBlock(@{});
        }
    }
}

#pragma mark - Child/Parent VC
- (void)showFlutterViewOverSnapshot {
    WDFlutterViewController *flutterVC = [WDFlutterViewWrapperController flutterVC];
    if (self.lastSnapshot) {
        [self.view bringSubviewToFront:self.fakeSnapImgView];
    }
    flutterVC.view.frame = self.view.bounds;
    flutterVC.view.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.view bringSubviewToFront:flutterVC.view];
        // pop时清除，
        if (self.currentNav.topViewController == self) {
            self.lastSnapshot = nil;
        }
    });
}

- (void)addChildFlutterVC {
    WDFlutterViewController *flutterVC = [WDFlutterViewWrapperController flutterVC];
    if (self == flutterVC.parentViewController) {
        [self showFlutterViewOverSnapshot];
        return;
    }
    if (nil != flutterVC.parentViewController) {
        [self removeChildFlutterVC];
    }
    [self.view addSubview:flutterVC.view];
    [self addChildViewController:flutterVC];
    [self showFlutterViewOverSnapshot];
}

- (void)removeChildFlutterVC{
    WDFlutterViewController *flutterVC = [WDFlutterViewWrapperController flutterVC];
    //Remove VC
    [flutterVC removeFromParentViewController];
    [flutterVC.view removeFromSuperview];
}

- (void)saveSnapshot{
    WDFlutterViewController *flutterVC = [WDFlutterViewWrapperController flutterVC];
    if(flutterVC.parentViewController != self)
        return;
    if(self.lastSnapshot == nil){
        UIGraphicsBeginImageContextWithOptions([UIScreen mainScreen].bounds.size, YES, 0);
        [flutterVC.view drawViewHierarchyInRect:flutterVC.view.bounds afterScreenUpdates:NO];
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

- (void)flutterViewDidRender {
    [WDFlutterURLRouter onFlutterViewRender];
}

+ (WDFlutterViewController *)flutterVC {
    static dispatch_once_t onceToken;
    static WDFlutterViewController *flutterVC;
    if (flutterVC) return flutterVC;
    dispatch_once(&onceToken, ^{
        printf("init flutter engine");
        flutterVC = [[WDFlutterViewController alloc] initWithProject:nil nibName:nil bundle:nil];
        [flutterVC setFlutterViewDidRenderCallback:^{
            if (flutterVC.parentViewController) {
                [(WDFlutterViewWrapperController *)flutterVC.parentViewController flutterViewDidRender];
            }
        }];
    });
    return flutterVC;
}

@end

@implementation WDFlutterRouteOptions

- (NSDictionary *)toDictionary {
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    if (_args) {
        [dictionary setObject:_args forKey:@"args"];
    }
    [dictionary setObject:_pageName ? :@"" forKey:@"pageName"];
    [dictionary setObject:_nativePageId ? :@"" forKey:@"nativePageId"];
    return dictionary;
}

@end
