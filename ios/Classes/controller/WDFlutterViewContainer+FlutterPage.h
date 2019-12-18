//
//  WDFlutterViewContainer+FlutterPage.h
//  hybrid_router
//
//  Created by blackox626 on 2019/12/6.
//

#import "WDFlutterViewContainer.h"
#import "WDFlutterPageLifeCircle.h"

NS_ASSUME_NONNULL_BEGIN

@interface WDFlutterViewContainer (FlutterPage) <WDFlutterPageLifeCircle>

- (BOOL)willPop;

@end

NS_ASSUME_NONNULL_END
