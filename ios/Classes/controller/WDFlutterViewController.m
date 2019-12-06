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

#import "WDFlutterViewController.h"
#import "WDFlutterEngine.h"
#import "HybridRouterPlugin.h"

static BOOL onceDisplaySplashView = NO;

@interface WDFlutterViewController ()
@property(nonatomic, strong) UIView *splashView;
@end

@implementation WDFlutterViewController

- (id)init {
    //前一个flutter vc不在更新
    WDFlutterViewController *fvc = (WDFlutterViewController *) WDFlutterEngine.sharedInstance.engine.viewController;
    if(fvc) {
        [fvc surfaceUpdated:NO];
    }
    
    self = [super initWithEngine:WDFlutterEngine.sharedInstance.engine nibName:nil bundle:nil];
    if (self) {
       
    }
    return self;
}

- (void)dealloc {
    //[WDFlutterEngine.sharedInstance detach];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    if (!onceDisplaySplashView) {
        if (!self.splashScreenView) {
            self.splashScreenView = self.splashView;
        }
        onceDisplaySplashView = YES;
    }
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    [[WDFlutterEngine.sharedInstance.engine lifecycleChannel] sendMessage:@"AppLifecycleState.resumed"];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (void)viewWillAppear:(BOOL)animated {
    NSLog(@"---viewWillAppear %@",self);
    [super viewWillAppear:animated];

    self.navigationController.navigationBar.hidden = YES;

    if (self.viewWillAppearBlock) {
        self.viewWillAppearBlock();
        self.viewWillAppearBlock = nil;
    }
    
    [WDFlutterRouter.sharedInstance add:self];
}

- (void)viewDidAppear:(BOOL)animated {
    NSLog(@"---viewDidAppear %@",self);
    
    FlutterViewController *fltvc = WDFlutterEngine.sharedInstance.engine.viewController;
    
    //fltvc 不是 当面页面  需要重新 atach 当面页面，并通知 flutter 当前页面resumed 否则会闪屏
    if(fltvc != self) {
        [(WDFlutterViewController *)fltvc surfaceUpdated:NO];
        WDFlutterEngine.sharedInstance.engine.viewController = self;
        [[HybridRouterPlugin sharedInstance] invokeFlutterMethod:@"onNativePageResumed" arguments:@{@"nativePageId": self.options.nativePageId}];
    }
    
    [self surfaceUpdated:YES];
    
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    NSLog(@"---viewWillDisappear %@",self);
    [super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated {
    NSLog(@"---viewDidDisappear %@",self);
    [super viewDidDisappear:animated];
}

- (void)didMoveToParentViewController:(UIViewController *)parent {
    [super didMoveToParentViewController:parent];
    if (parent == nil) {
        //当前controllere被remove
        [self onNativePageFinished];
    }
}

- (void)onNativePageFinished {
    [[HybridRouterPlugin sharedInstance] invokeFlutterMethod:@"onNativePageFinished" arguments:@{@"nativePageId": self.options.nativePageId}];
    
    [WDFlutterRouter.sharedInstance remove:self];
    
    [[HybridRouterPlugin sharedInstance] popDone:self.options.nativePageId];
}

- (UIView *)splashView {
    if (!_splashView) {
        _splashView = [[UIView alloc] initWithFrame:self.view.bounds];
        _splashView.backgroundColor = [UIColor whiteColor];
    }
    return _splashView;
}

@end
