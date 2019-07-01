//
//  WDFlutterEngine.h
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import <Foundation/Foundation.h>
#import "WDFlutterViewProvider.h"

@protocol WDFlutterEngineDelegate <NSObject>

@required
- (UINavigationController *_Nonnull)appNavigationController;

@end

NS_ASSUME_NONNULL_BEGIN

@interface WDFlutterEngine : NSObject <WDFlutterViewProvider>

@property (nonatomic,weak) id<WDFlutterEngineDelegate>delegate;

+ (instancetype)sharedInstance;

@end

NS_ASSUME_NONNULL_END
