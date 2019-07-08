//
//  WDFlutterPluginRigstrant.h
//  hybrid_router
//
//  Created by blackox626 on 2019/5/28.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

@interface WDFlutterPluginRigstrant : NSObject

+ (void)registerWithRegistry:(NSObject <FlutterPluginRegistry> *)registry;

+ (void)registePlugin:(Class <FlutterPlugin>)plugin;

@end

NS_ASSUME_NONNULL_END
