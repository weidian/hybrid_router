//
//  WDFlutterViewContainer+FlutterPage.m
//  hybrid_router
//
//  Created by blackox626 on 2019/12/6.
//

#import "WDFlutterViewController+FlutterPage.h"
#import "WDFlutterViewController.h"

@implementation WDFlutterViewController (FlutterPage)

- (void)flutterPagePushed:(NSString *)pageName {
    self.flutterPageCount++;
    if (self.flutterPageCount > 1) {
        self.navigationController.interactivePopGestureRecognizer.enabled = NO;
    }
}

- (void)flutterPageRemoved:(NSString *)pageName {
    self.flutterPageCount--;
    if (self.flutterPageCount <= 1) {
        self.navigationController.interactivePopGestureRecognizer.enabled = YES;
    }
}

@end
