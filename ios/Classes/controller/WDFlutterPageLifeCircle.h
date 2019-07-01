//
//  WDFlutterPageLifeCircle.h
//  hybrid_router
//
//  Created by blackox626 on 2019/7/1.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol WDFlutterPageLifeCircle <NSObject>

@optional
- (void)flutterPagePushed;
- (void)flutterPageResume;
- (void)flutterPagePause;
- (void)flutterPageRemoved;

@end

NS_ASSUME_NONNULL_END
