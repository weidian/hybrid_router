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

typedef void (^FlutterViewWillAppearBlock) (void);

@interface WDFlutterViewContainer ()

@property (nonatomic, strong) NSString *pageId;

@property (nonatomic, copy) FlutterViewWillAppearBlock viewWillAppearBlock;

@end

@implementation WDFlutterViewContainer

- (id)init {
    //前一个fluttervc detach ，attach当前页面
    [WD_FLUTTER_ENGINE prepare];
    self = [super initWithEngine:WDFlutterEngine.sharedInstance.engine nibName:nil bundle:nil];
    if (self) {
    }
    return self;
}

static long long fTag = 0;

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.view.backgroundColor = [UIColor whiteColor];
    
    self.pageId = @(fTag++).stringValue;
    
    self.viewWillAppearBlock = ^() {
        self.options.nativePageId = self.pageId;
        
        NSDictionary *options = [self.options toDictionary];
        if (!HybridRouterPlugin.sharedInstance.initialized && !HybridRouterPlugin.sharedInstance.mainEntryParams) {
            HybridRouterPlugin.sharedInstance.mainEntryParams = options;
        } else {
            [HybridRouterPlugin.sharedInstance invokeFlutterMethod:@"pushFlutterPage" arguments:options];
        }
    };
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    [WD_FLUTTER_ENGINE resume];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];

    if (self.viewWillAppearBlock) {
        self.viewWillAppearBlock();
        self.viewWillAppearBlock = nil;
        self.navigationController.interactivePopGestureRecognizer.enabled = YES;
    }
}

- (void)viewDidAppear:(BOOL)animated {
    //fltvc 不是当面页面, 需要重新attach当面页面 && 通知 flutter 当前页面resumed
    if ([WD_FLUTTER_ENGINE flutterViewController] != self) {
        [WD_FLUTTER_ENGINE attach:self];
        //更新viewport
        [self viewDidLayoutSubviews];
        [[HybridRouterPlugin sharedInstance] invokeFlutterMethod:@"onNativePageResumed"
                                                       arguments:@{@"nativePageId": self.options.nativePageId}
                                                          result:^(id result) {
            [self surfaceUpdated:YES];
        }];
    } else {
        [self surfaceUpdated:YES];
    }

//    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
//        //resumed 之后执行 否则会闪屏
//        [self surfaceUpdated:YES];
//    });
    
    [super viewDidAppear:animated];
    
    if (self.flutterPageCount > 1) {
        self.navigationController.interactivePopGestureRecognizer.enabled = NO;
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    self.navigationController.interactivePopGestureRecognizer.enabled = YES;
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    //处理下present 页面卡死的情况
    if ([WD_FLUTTER_ENGINE flutterViewController] != self) {
        [WD_FLUTTER_ENGINE resume];
        [(WDFlutterViewContainer *)[WD_FLUTTER_ENGINE flutterViewController] surfaceUpdated:YES];
    } else {
      [WD_FLUTTER_ENGINE detach];
    }
}

- (void)dealloc {
    [self onNativePageFinished];
}

- (void)onNativePageFinished {
    [[HybridRouterPlugin sharedInstance] invokeFlutterMethod:@"onNativePageFinished"
                                                   arguments:@{@"nativePageId": self.options.nativePageId}];
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
