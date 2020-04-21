//
//  WDFlutterEngine.h
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import <Foundation/Foundation.h>
#import "WDFlutterEngineProvider.h"

@class FlutterEngine;

NS_ASSUME_NONNULL_BEGIN

@interface WDFlutterEngine : NSObject <WDFlutterEngineProvider>

@property(nonatomic, strong) FlutterEngine *engine;

+ (instancetype)sharedInstance;

@end

NS_ASSUME_NONNULL_END
