#include "AppDelegate.h"
#include "DemoViewController.h"
#import "WDFlutterRouter.h"

@interface AppDelegate () <WDFlutterRouterDelegate>

@end

@implementation AppDelegate

- (BOOL)          application:(UIApplication *)application
didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
//    [GeneratedPluginRegistrant registerWithRegistry:self];
//    Override point for customization after application launch.

    WDFlutterRouter.sharedInstance.delegate = self;

//    WDFlutterRouteOptions *options = [[WDFlutterRouteOptions alloc] init];
//    options.pageName = @"example";
//    options.args = @"EXAMPLE";
//    options.isTab = TRUE;
//
//    WDFlutterViewContainer *fvc = [[WDFlutterViewContainer alloc] init];
//    fvc.hidesBottomBarWhenPushed = NO;
//    fvc.routeOptions = options;
//    UINavigationController *nav0 = [[UINavigationController alloc] initWithRootViewController:fvc];
//    nav0.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"flutter" image:nil tag:0];

    DemoViewController *vc = [[DemoViewController alloc] init];
    UINavigationController *nav1 = [[UINavigationController alloc] initWithRootViewController:vc];
    nav1.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"native" image:nil tag:1];

//    WDFlutterRouteOptions *options_ = [[WDFlutterRouteOptions alloc] init];
//    options_.pageName = @"example";
//    options_.args = @"EXAMPLE";
//    options_.isTab = TRUE;
//
//    WDFlutterViewContainer *fvc_ = [[WDFlutterViewContainer alloc] init];
//    fvc_.hidesBottomBarWhenPushed = NO;
//    fvc_.routeOptions = options_;
//    UINavigationController *nav2 = [[UINavigationController alloc] initWithRootViewController:fvc_];
//    nav2.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"flutter2" image:nil tag:2];

    UITabBarController *tabVC = [[UITabBarController alloc] init];
    //tabVC.viewControllers = @[nav0, nav1, nav2];
    tabVC.viewControllers = @[nav1];

    self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    self.window.rootViewController = nav1;
    [self.window makeKeyAndVisible];
    return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

#pragma mark -- WDFlutterRouterDelegate

- (UINavigationController *)appNavigationController {
    UINavigationController *nav = (UINavigationController *) [UIApplication sharedApplication].delegate.window.rootViewController;
    return nav;
}

- (void)openNativePage:(NSString *)page params:(id)params transitionType:(WDFlutterRouterTransitionType)type {
    [[self appNavigationController] pushViewController:[[DemoViewController alloc] init] animated:YES];
}

@end
