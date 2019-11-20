# hybrid_router

一个 Flutter 混合栈插件，无需修改业务中 `Navigator` 相关代码，可以帮你轻松的将 Flutter 混合栈能力加入到现有的 flutter 工程，你可以像开发原生 flutter app 一样开发混合栈 app

# 特点
* android 无需使用截图
* 可以直接使用系统原生的 api，包括打开页面结果获取，入侵程度小
* 支持原生的 Flutter 跳转方式（不借助 native 容器跳转）

# Flutter SDK 要求
master 分支需要 `v1.3.0` 以上，`v1.2.2` 无法工作（flutter engine android 部分有 bug）。如果你是使用 `v1.0.0` 版本的，请切换到 **support/v1.0.0** 分支，此分支下默认的 `pushNamed` 无法传递参数，不过可以使用混合栈的 `HybridNavigator`

# Dart 集成
## 初始化
```dart
void main() {
  // 创建 root navigator 需要的 observer，混合栈是根据此 observer 来完成
  // 一些生命周期监听的
  final GlobalKey<NativeContainerManagerState> managerKey = GlobalKey();
  NativeContainerManager manager = HybridNavigator.init(
      key: managerKey,
      backgroundBuilder: (context) {
        return EmptyPage();
      },
      // 调用系统 api 的默认打开行为（通过新的 native 容器打开）
      defaultPushType: HybridPushType.Native,
      // 可以监听所有 HybridNavigator 中的 Route
      pageObserver: [],
      routes: {
        // 路由表
        "example": (context, argument) {
          return ExamplePage(
            title: argument as String,
          );
        }
      },
      // 404 页面
      unknownRouteBuilder: <T>(RouteSettings settings) {
        return MaterialPageRoute<T>(
          builder: (context) {
            return PageNotFound();
          },
          settings: settings,
        );
      });

  runApp(MaterialApp(
    theme: ThemeData(
      brightness: Brightness.light,
    ),
    // 这里设置 home 为 创建好的 manager
    home: manager,
  ));
}
```
## 使用
### 打开 Flutter 页面
#### 1. 通过 `pushNamed` 跳转
```dart
Navigator.of(context).pushNamed('example', arguments: 'message');
```
#### 2. 通过 `push` 跳转
```dart
Navigator.of(context).push(
  MaterialPageRoute(
    builder: (context) {
      return ExamplePage(title: "Jump from flutter",);
    }
  )
);
```
### 指定跳转方式（Native or Flutter）
```dart
// 通过 push 指定跳转方式，需要使用 HybridPageRoute
Navigator.of(context).push(HybridPageRoute(
  pushType: HybridPushType.Flutter,
  builder: (context) {
    return ExamplePage(title: 'Jump from flutter',);
  }
));
// 使用 pushNamed 来实现指定，需要使用 HybridNavigator
HybridNavigator.of(context).pushNamed('example', arguments: 'message',
  pushType: HybridPushType.Flutter);
```
### 处理页面返回结果
```dart
// 普通的 pop 就行
Navigator.of(context).pop('message');
```
### 跳转到 Native 页面
```dart
// 需要使用混合栈接口
HybridNavigator.of(context)
  .openNativePage(url: 'https://www.baidu.com', args: 'message');
```


# Android 集成
## 接入
初始化定制行为：用于定制一些行为，比如 Flutter 请求打开 native page route 的时候定制路由行为。不设置表示使用默认的配置
```java
// 可以添加一些自定义的行为
FlutterManager.getInstance().setFlutterWrapConfig(new EmptyFlutterWrapConfig() {

    @Override
    public void postFlutterApplyTheme(@NonNull IFlutterNativePage nativePage) {
        // 修改当前沉浸式主题的背景色为透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                nativePage.getContext() instanceof Activity) {
            Window window = ((Activity)nativePage.getContext()).getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public boolean onFlutterPageRoute(@NonNull IFlutterNativePage nativePage,
                                      @Nullable FlutterRouteOptions routeOptions, int requestCode) {
        // 自定义flutter 页面的跳转
        Intent intent = HybridFlutterActivity.newBuilder().route(routeOptions)
                .buildIntent(nativePage.getContext());
        nativePage.startActivityForResult(intent, requestCode);
        return true;
    }
});
```
## 使用
### 打开 flutter 页面
```java
// example 为 flutter 中 routes 对应的 key
Intent intent = HybridFlutterActivity.newBuilder()
  .route(new FlutterRouteOptions.Builder("example")
    .setArgs("Jump From Main").build())
    .buildIntent(MainActivity.this);
startActivity(intent);
```

# iOS 集成
## 初始化
初始化的时候设置代理类
```objc
/**
 初始化

 @param delegate 实现<WDFlutterURLRouterDelegate>的代理
 */
+ (void)setupWithDelegate:(id<WDFlutterURLRouterDelegate>)delegate;
```
在代理类中实现以下方法
```objc
/**
 返回导航对象的回调，用于打开flutter页面

 @return 导航对象
 */
- (UIViewController *)flutterCurrentController;
```
```objc
/**
 flutter打开native页面的回调

 @param page native的页面名
 @param params 页面参数
 */
- (void)openNativePage:(NSString *)page params:(NSDictionary *)params;
```
可选的代理方法：
```objc
/**
 获取flutter页面的容器，可以通过继承WDFlutterViewWrapperController来定制容器。默认使用WDFlutterViewWrapperController。

 @param routeOptions 路由参数
 @return flutter页面容器
 */
- (WDFlutterViewWrapperController *)flutterWrapperController:(WDFlutterRouteOptions *)routeOptions;
```
## 使用
### 打开flutter页面
```objc
/**
 打开flutter页面

 @param page flutter页面名
 @param params 传给页面的参数
 @param result 页面返回时的回调
 */
+ (void)openFlutterPage:(NSString *)page params:(NSDictionary *)params result:(void (^)(id))result;
```

# License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE) file for details