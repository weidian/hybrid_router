//
//  WDFStackManager.m
//  hybrid_router
//
//  Created by shazhou on 2019/4/25.
//

#import "WDFStackManager.h"

@implementation WDFStackManager

+ (void)setupWithDelegate:(id<WDFlutterURLRouterDelegate>)delegate {
    [WDFlutterURLRouter sharedInstance].delegate = delegate;
}

+ (void)openFlutterPage:(NSString *)page params:(NSDictionary *)params result:(void (^)(id))result {
    [WDFlutterURLRouter openFlutterPage:page params:params result:result];
}

@end
