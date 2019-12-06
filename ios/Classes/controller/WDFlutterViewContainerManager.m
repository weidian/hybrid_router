//
//  WDFlutterViewContainerManager.m
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import "WDFlutterViewContainerManager.h"

@interface WDFlutterViewContainerManager ()
@property(nonatomic, strong) NSMutableDictionary *containers;
@end

@implementation WDFlutterViewContainerManager

- (instancetype)init {
    if (self = [super init]) {
        _containers = [NSMutableDictionary dictionary];
    }
    return self;
}

- (BOOL)contains:(WDFlutterViewController *)container {
    if (container) {
        return _containers[container.options.nativePageId] ? YES : NO;
    }
    return NO;
}

- (void)add:(WDFlutterViewController *)container {
    if ([self contains:container]) {
        return;
    }
    _containers[container.options.nativePageId] = container;
}

- (void)remove:(WDFlutterViewController *)container {
    if (![self contains:container]) {
        return;
    }
    [_containers removeObjectForKey:container.options.nativePageId];
}

- (WDFlutterViewContainer *)find:(NSString *)pageId {
    return _containers[pageId];
}

@end
