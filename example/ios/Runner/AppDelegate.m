#include "AppDelegate.h"
#include "GeneratedPluginRegistrant.h"
#include "DemoViewController.h"
#import "WDFStackManager.h"
#import "WDFlutterViewContainer.h"

@interface AppDelegate()<WDFlutterURLRouterDelegate>

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  //[GeneratedPluginRegistrant registerWithRegistry:self];
  // Override point for customization after application launch.
  
  [WDFStackManager setupWithDelegate:self];

  WDFlutterRouteOptions *options = [[WDFlutterRouteOptions alloc] init];
  options.pageName = @"example";
  options.args = @"EXAMPLE";
  options.isTab = TRUE;
  
  WDFlutterViewContainer *fvc = [[WDFlutterViewContainer alloc] init];
  fvc.hidesBottomBarWhenPushed = NO;
  fvc.routeOptions = options;
  UINavigationController *nav0 = [[UINavigationController alloc] initWithRootViewController:fvc];
  nav0.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"flutter" image:nil tag:1];

  DemoViewController *vc = [[DemoViewController alloc] init];
  UINavigationController *nav1 = [[UINavigationController alloc] initWithRootViewController:vc];
  nav1.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"native" image:nil tag:0];

  UITabBarController *tabVC = [[UITabBarController alloc] init];
  tabVC.viewControllers = @[nav0,nav1];
  
  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  self.window.rootViewController = tabVC;
  [self.window makeKeyAndVisible];
  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

#pragma mark - WDFlutterURLRouterDelegate
- (UIViewController *)flutterCurrentController {
    UITabBarController *tabVC = (UITabBarController *) [UIApplication sharedApplication].delegate.window.rootViewController;
    return tabVC.viewControllers[tabVC.selectedIndex];
}

- (void)openNativePage:(NSString *)page params:(NSDictionary *)params {
    [(UINavigationController *)[self flutterCurrentController] pushViewController:[[DemoViewController alloc] init] animated:YES];
}

@end
