//
//  WDFlutterViewProvider.h
//  hybrid_router
//
//  Created by blackox626 on 2019/6/27.
//

#import <Foundation/Foundation.h>

@class FlutterViewController;

NS_ASSUME_NONNULL_BEGIN

@protocol WDFlutterViewProvider <NSObject>

@required
- (FlutterViewController *)viewController;

@end

NS_ASSUME_NONNULL_END
