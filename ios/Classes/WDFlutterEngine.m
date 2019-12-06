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
@property(nonatomic, strong) FlutterViewController *dummy;
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
        
        _dummy = [[WDFlutterViewController alloc] initWithEngine:_engine
                                                         nibName:nil
                                                          bundle:nil];
        
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

#pragma mark -- WDFlutterEngineProvider

- (FlutterViewController *)flutterViewController {
    return _engine.viewController;
}

- (void)prepare {
    WDFlutterViewController *fvc = (WDFlutterViewController *) _engine.viewController;
    if(fvc) {
        [fvc surfaceUpdated:NO];
    }
}

- (void)atach:(FlutterViewController *)vc {
    if(_engine.viewController != vc) {
        [(WDFlutterViewController *)_engine.viewController surfaceUpdated:NO];
        _engine.viewController = vc;
    }
}

- (void)detach {
    if(_engine.viewController != _dummy){
        _engine.viewController = _dummy;
    }
}

@end
