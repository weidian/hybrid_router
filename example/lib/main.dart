import 'package:flutter/material.dart';

import 'package:hybrid_router/hybrid_router.dart';
import 'example.dart';

void main() {
  NavigatorObserver observer = HybridNavigator.init(
      defaultPushType: HybridPushType.Native,
      observers: [_TestNavigatorObserver()],
      routes: {
        "example": (context, argument) {
          return ExamplePage(
            title: argument as String,
          );
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
    navigatorObservers: [observer],
    theme: ThemeData(
      brightness: Brightness.light,
    ),
    home: EmptyPage(),
  ));
  HybridNavigator.startInitRoute();
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
