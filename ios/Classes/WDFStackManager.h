//
//  WDFStackManager.h
//  hybrid_router
//
//  Created by shazhou on 2019/4/25.
//

#import <Foundation/Foundation.h>
#import "WDFlutterURLRouter.h"

NS_ASSUME_NONNULL_BEGIN

@interface WDFStackManager : NSObject

/**
 初始化

 @param delegate 实现<WDFlutterURLRouterDelegate>的代理
 */
+ (void)setupWithDelegate:(id<WDFlutterURLRouterDelegate>)delegate;

/**
 打开flutter页面

 @param page flutter页面名
 @param params 传给页面的参数
 @param result 页面返回时的回调
 */
+ (void)openFlutterPage:(NSString *)page params:(NSDictionary *)params result:(void (^)(id))result;

@end

NS_ASSUME_NONNULL_END
