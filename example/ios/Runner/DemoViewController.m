//
//  DemoViewController.m
//  Runner
//
//  Created by shazhou on 2019/2/27.
//  Copyright © 2019 The Chromium Authors. All rights reserved.
//

#import "DemoViewController.h"
#import "WDFlutterRouter.h"

@interface DemoViewController ()

@end

@implementation DemoViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = [UIColor whiteColor];
}

- (void)viewWillAppear:(BOOL)animated {
    self.navigationController.navigationBar.hidden = NO;
}

- (void)loadView {
    [super loadView];
    self.title = @"native";

    UIButton *btn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 200, 40)];
    [btn setTitle:@"Click to jump Flutter" forState:UIControlStateNormal];
    [self.view addSubview:btn];
    [btn setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [btn setCenter:self.view.center];
    [btn addTarget:self action:@selector(onJumpFlutterPressed) forControlEvents:UIControlEventTouchUpInside];

    //[self onJumpFlutterPressed];

    UIButton *btn_ = [[UIButton alloc] initWithFrame:CGRectMake(0, 150, self.view.frame.size.width, 40)];
    [btn_ setTitle:@"jump Flutter Tab" forState:UIControlStateNormal];
    [self.view addSubview:btn_];
    [btn_ setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    //[btn_ setCenter:self.view.center];
    [btn_ addTarget:self action:@selector(onJumpFlutterTabPressed) forControlEvents:UIControlEventTouchUpInside];

//    UIButton *btn__ = [[UIButton alloc] initWithFrame:CGRectMake(0, 200, self.view.frame.size.width, 40)];
//    [btn__ setTitle:@"jump native Tab" forState:UIControlStateNormal];
//    [self.view addSubview:btn__];
//    [btn__ setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
//    //[btn_ setCenter:self.view.center];
//    [btn__ addTarget:self action:@selector(onJumpNativeTabPressed) forControlEvents:UIControlEventTouchUpInside];
}

- (void)onJumpFlutterPressed {
    [WDFlutterRouter.sharedInstance openFlutterPage:@"example"
                                             params:@"EXAMPLE"
                                             result:^(NSDictionary *data) {
                                                 NSLog(@"%@", data[@"data"]);
                                             }];

    /*[WDFlutterRouter.sharedInstance openFlutterPage:@"example"
                                             params:@"EXAMPLE"
                                             result:^(NSDictionary *data) {
                                                 NSLog(@"%@", data[@"data"]);
                                             }
                                              modal:YES
                                           animated:YES];*/
}

- (void)onJumpFlutterTabPressed {
    UITabBarController *tabVc = (UITabBarController *) [UIApplication sharedApplication].delegate.window.rootViewController;

    tabVc.selectedIndex = 0;
}

//- (void)onJumpNativeTabPressed {
//    UITabBarController *tabVc = (UITabBarController *)[UIApplication sharedApplication].delegate.window.rootViewController;
//
//    tabVc.selectedIndex = 1;
//}
/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
