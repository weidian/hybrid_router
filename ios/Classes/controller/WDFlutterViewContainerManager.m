//
//  WDFlutterViewContainerManager.m
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import "WDFlutterViewContainerManager.h"
#import "WDFlutterViewContainer.h"

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

- (BOOL)contains:(WDFlutterViewContainer *)container {
    if (container) {
        return _containers[container.routeOptions.nativePageId] ? YES : NO;
    }
    return NO;
}

- (void)add:(WDFlutterViewContainer *)container {
    if ([self contains:container]) {
        return;
    }
    _containers[container.routeOptions.nativePageId] = container;
}

- (void)remove:(WDFlutterViewContainer *)container {
    if (![self contains:container]) {
        return;
    }
    [_containers removeObjectForKey:container.routeOptions.nativePageId];
}

- (WDFlutterViewContainer *)find:(NSString *)pageId {
    return _containers[pageId];
}

@end
