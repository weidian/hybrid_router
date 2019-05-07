#include "AppDelegate.h"
#include "GeneratedPluginRegistrant.h"
#include "DemoViewController.h"
#import "WDFStackManager.h"

@interface AppDelegate()<WDFlutterURLRouterDelegate>

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  [GeneratedPluginRegistrant registerWithRegistry:self];
  // Override point for customization after application launch.
    
    
    UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:[[DemoViewController alloc] init]];
    self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    self.window.rootViewController = nav;
    [self.window makeKeyAndVisible];
    
    [WDFStackManager setupWithDelegate:self];
    
  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

#pragma mark - XURLRouterDelegate
- (UIViewController *)flutterCurrentController {
    return [UIApplication sharedApplication].delegate.window.rootViewController;
}

- (void)openNativePage:(NSString *)page params:(NSDictionary *)params {
    [(UINavigationController *)[self flutterCurrentController] pushViewController:[[DemoViewController alloc] init] animated:YES];
}

@end
