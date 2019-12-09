//
//  WDFlutterViewContainerManager.m
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import "WDFlutterViewContainerManager.h"
#import "WDFlutterViewController.h"

@interface WDFlutterViewContainerManager ()
@property(nonatomic, strong) NSMutableDictionary *controllers;
@end

@implementation WDFlutterViewContainerManager

- (instancetype)init {
    if (self = [super init]) {
        _controllers = [NSMutableDictionary dictionary];
    }
    return self;
}

- (BOOL)contains:(WDFlutterViewController *)controller {
    if (controller) {
        return _controllers[controller.options.nativePageId] ? YES : NO;
    }
    return NO;
}

- (void)add:(WDFlutterViewController *)controller {
    if ([self contains:controller]) {
        return;
    }
    _controllers[controller.options.nativePageId] = controller;
}

- (void)remove:(WDFlutterViewController *)controller {
    if (![self contains:controller]) {
        return;
    }
    [_controllers removeObjectForKey:controller.options.nativePageId];
}

- (WDFlutterViewController *)find:(NSString *)pageId {
    return _controllers[pageId];
}

@end
