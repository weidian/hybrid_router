//
//  MyFlutterViewController.m
//  Runner
//
//  Created by blackox626 on 2019/12/9.
//  Copyright © 2019 The Chromium Authors. All rights reserved.
//

#import "MyFlutterViewController.h"

@implementation MyFlutterViewController

- (id)init {
    self = [super init];
    if (self) {
        self.hidesBottomBarWhenPushed = YES;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.edgesForExtendedLayout = UIRectEdgeNone;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.navigationController.navigationBar.hidden = YES;
}

@end
