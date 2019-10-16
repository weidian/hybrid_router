//
//  WDFlutterEngine.m
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import <Flutter/Flutter.h>
#import "WDFlutterEngine.h"
#import "WDFlutterViewController.h"
#import "WDFlutterPluginRigstrant.h"

@interface WDFlutterEngine ()
@property(nonatomic, strong) FlutterViewController *viewController;
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
        
        FlutterEngine *engine = [[FlutterEngine alloc] initWithName:@"share_engine" project:nil];
        [engine runWithEntrypoint:nil];
        
        _engine = engine;
        
//        _viewController = [[WDFlutterViewController alloc] initWithProject:nil nibName:nil bundle:nil];
//        [_viewController view];
        Class clazz = NSClassFromString(@"GeneratedPluginRegistrant");
        if (clazz) {
            if ([clazz respondsToSelector:NSSelectorFromString(@"registerWithRegistry:")]) {
                [clazz performSelector:NSSelectorFromString(@"registerWithRegistry:")
                            withObject:_engine];
            }
        }
        [WDFlutterPluginRigstrant registerWithRegistry:_engine];
    }

    return self;
#pragma clang diagnostic pop
}

#pragma mark -- WDFlutterViewProvider

- (nonnull FlutterViewController *)viewController {
    return _viewController;
}

@end
