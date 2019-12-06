//
//  WDFlutterEngine.h
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import <Foundation/Foundation.h>
#import "WDFlutterViewProvider.h"

@class FlutterEngine;

NS_ASSUME_NONNULL_BEGIN

@interface WDFlutterEngine : NSObject <WDFlutterViewProvider>

@property(nonatomic, strong) FlutterEngine *engine;

+ (instancetype)sharedInstance;

- (void)detach;

@end

NS_ASSUME_NONNULL_END
