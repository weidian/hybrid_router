//
//  WDFlutterViewContainerManager.h
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@class WDFlutterViewContainer;

@interface WDFlutterViewContainerManager : NSObject

- (void)add:(WDFlutterViewContainer *)container;
- (void)remove:(WDFlutterViewContainer *)container;
- (WDFlutterViewContainer *)find:(NSString *)pageId;

@end

NS_ASSUME_NONNULL_END
