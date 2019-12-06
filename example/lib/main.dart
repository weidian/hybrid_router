import 'package:flutter/material.dart';

import 'package:hybrid_router/hybrid_router.dart';
import 'example.dart';

void main() {
  final GlobalKey<NativeContainerManagerState> managerKey = GlobalKey();
  var manager = HybridNavigator.init(
      key: managerKey,
      backgroundBuilder: (context) {
        return EmptyPage();
      },
      defaultPushType: HybridPushType.Native,
      pageObserver: [_TestNavigatorObserver()],
      routes: {
        "example": (context, argument) {
          return ExamplePage(
            title: argument as String,
          );
        },
        "tab_example": (context, argument) {
          return TabExamplePage();
        }
      },
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
    home: manager,
  ));
  var errorCallback = FlutterError.onError;
  FlutterError.onError = (details) {
    FlutterError.resetErrorCount();
    errorCallback(details);
  };
}

class EmptyPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(color: Colors.white);
  }
}

class PageNotFound extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Text(
          '页面丢失!',
          style: TextStyle(color: Colors.red),
        ),
      ),
    );
  }
}

class _TestNavigatorObserver extends HybridNavigatorObserver {
  @override
  void didPush(Route route, Route previousRoute) {
    super.didPush(route, previousRoute);
    print('onPush: ${route?.settings?.name}');
  }

  @override
  void didPop(Route route, Route previousRoute) {
    super.didPop(route, previousRoute);
    print('onPop: ${route?.settings?.name}');
  }
}
