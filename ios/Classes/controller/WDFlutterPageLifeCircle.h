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

- (void)nativePageWillRemove:(id)result;

- (void)nativePageRemoved:(id)result;

- (void)nativePageResume;

- (void)nativePageCreate;

- (void)nativePagePause;

- (void)flutterPagePushed:(NSString *)pageName;

- (void)flutterPageResume:(NSString *)pageName;

- (void)flutterPagePause:(NSString *)pageName;

- (void)flutterPageRemoved:(NSString *)pageName;

@end

NS_ASSUME_NONNULL_END
