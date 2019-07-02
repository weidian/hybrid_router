//
//  WDFlutterEngine.m
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import "WDFlutterEngine.h"
#import "WDFlutterViewController.h"
#import "WDFlutterPluginRigstrant.h"

@interface WDFlutterEngine()
@property (nonatomic,strong) WDFlutterViewController *viewController;
@end

@implementation WDFlutterEngine

+ (instancetype)sharedInstance {
    static WDFlutterEngine *sInstance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sInstance = [WDFlutterEngine new];
    });
    return sInstance;
}

- (instancetype)init {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
    
    if (self = [super init]) {
        _viewController = [[WDFlutterViewController alloc] initWithProject:nil nibName:nil bundle:nil];
        [_viewController view];
        Class clazz = NSClassFromString(@"GeneratedPluginRegistrant");
        if (clazz) {
            if ([clazz respondsToSelector:NSSelectorFromString(@"registerWithRegistry:")]) {
                [clazz performSelector:NSSelectorFromString(@"registerWithRegistry:")
                            withObject:_viewController];
            }
        }
        [WDFlutterPluginRigstrant registerWithRegistry:_viewController];
    }
    
    return self;
#pragma clang diagnostic pop
}

#pragma mark -- WDFlutterViewProvider

- (nonnull FlutterViewController *)viewController {
    return _viewController;
}

@end
