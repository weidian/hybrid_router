//
// Created by lm on 2018/11/15.
// Copyright (c) 2018 lm. All rights reserved.
//

#import "WDFlutterViewController.h"

static BOOL onceDisplaySplashView = NO;
@interface WDFlutterViewController ()
@property (nonatomic, assign) BOOL enableViewWillAppear;
@property (nonatomic, strong) UIView *splashView;
@end

@implementation WDFlutterViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.enableViewWillAppear = TRUE;
    if (!onceDisplaySplashView) {
        if (!self.splashScreenView) {
            self.splashScreenView = self.splashView;
        }
        onceDisplaySplashView = YES;
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (void)viewWillAppear:(BOOL)animated {
    if (self.enableViewWillAppear == FALSE) return;
    [super viewWillAppear:animated];
    self.enableViewWillAppear = FALSE;
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    self.enableViewWillAppear = TRUE;
}

- (UIView *)splashView {
    if (!_splashView) {
        _splashView = [[UIView alloc] initWithFrame:self.view.bounds];
        _splashView.backgroundColor = [UIColor whiteColor];
    }
    return _splashView;
}

@end
