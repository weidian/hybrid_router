//
//  WDFlutterPluginRigstrant.m
//  hybrid_router
//
//  Created by blackox626 on 2019/5/28.
//

#import "WDFlutterPluginRigstrant.h"

@interface WDFlutterPluginRigstrant ()
@property(nonatomic, strong) NSMutableArray<Class <FlutterPlugin>> *plugins;
@end

@implementation WDFlutterPluginRigstrant

+ (void)registerWithRegistry:(NSObject <FlutterPluginRegistry> *)registry {
    [[WDFlutterPluginRigstrant sharedInstance].plugins enumerateObjectsUsingBlock:^(Class <FlutterPlugin> _Nonnull plugin, NSUInteger idx, BOOL *_Nonnull stop) {
        [plugin registerWithRegistrar:[registry registrarForPlugin:NSStringFromClass(plugin)]];
    }];
}

+ (void)registePlugin:(Class <FlutterPlugin>)plugin {
    if ([[WDFlutterPluginRigstrant sharedInstance].plugins containsObject:plugin]) {
        return;
    }
    [[WDFlutterPluginRigstrant sharedInstance].plugins addObject:plugin];
}

+ (instancetype)sharedInstance {
    static WDFlutterPluginRigstrant *sInstance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sInstance = [WDFlutterPluginRigstrant new];
    });
    return sInstance;
}

- (id)init {
    self = [super init];
    if (self) {
        _plugins = [NSMutableArray array];
    }
    return self;
}

@end
