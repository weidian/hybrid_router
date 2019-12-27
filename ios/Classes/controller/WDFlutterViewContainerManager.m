//
//  WDFlutterViewContainerManager.m
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import "WDFlutterViewContainerManager.h"
#import "WDFlutterViewContainer.h"

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

- (BOOL)contains:(WDFlutterViewContainer *)controller {
    if (controller) {
        return _controllers[controller.options.nativePageId] ? YES : NO;
    }
    return NO;
}

- (void)add:(WDFlutterViewContainer *)container {
    if ([self contains:container]) {
        return;
    }
    _controllers[container.options.nativePageId] = container;
}

- (void)remove:(WDFlutterViewContainer *)container {
    if (![self contains:container]) {
        return;
    }
    [_controllers removeObjectForKey:container.options.nativePageId];
}

- (WDFlutterViewContainer *)find:(NSString *)pageId {
    return _controllers[pageId];
}

@end
