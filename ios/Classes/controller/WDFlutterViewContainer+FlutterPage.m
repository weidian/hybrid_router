//
//  WDFlutterViewContainer+FlutterPage.m
//  hybrid_router
//
//  Created by blackox626 on 2019/12/6.
//

#import "WDFlutterViewContainer+FlutterPage.h"
#import "WDFlutterViewContainer.h"
#import "HybridRouterPlugin.h"

@implementation WDFlutterViewContainer (FlutterPage)

- (void)nativePageWillRemove:(id)result {
    if (self.options.resultBlock) {
        if (result) {
            self.options.resultBlock(@{@"data": result});
        } else {
            self.options.resultBlock(@{});
        }
    }
}

- (void)nativePageRemoved:(id)result {
    [self nativePageWillRemove:result];
}

- (void)nativePageResume {

}

- (void)onNativePageCreate {
    
}

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
