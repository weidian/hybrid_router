/// 处理 返回键逻辑
import 'package:flutter/material.dart';

typedef OnBackPressedCallback = Future<bool> Function();

class BackPressedEmmiter {
  OnBackPressedCallback callback;

  Future<bool> emmit() async {
    if (callback != null) {
      return await callback();
    }
    return false;
  }
}

class BackPressed extends StatefulWidget {
  static BackPressedState of(BuildContext context,
      {bool root = false, bool nullOk = false}) {
    BackPressedState state = root
        ? context.rootAncestorStateOfType(TypeMatcher<BackPressedState>())
        : context.ancestorStateOfType(TypeMatcher<BackPressedState>());
    assert(() {
      if (state == null && !nullOk) {
        throw FlutterError(
            'BackPress operation requested with a context that does not include a BackPress.');
      }
      return true;
    }());
    return state;
  }

  final BackPressedEmmiter emmiter;

  final Widget child;

  const BackPressed({Key key, this.emmiter, this.child}) : super(key: key);

  @override
  BackPressedState createState() => BackPressedState();
}

class BackPressedState extends State<BackPressed> {
  List<OnBackPressedCallback> _onBackPressedList = [];

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _updateWidget();
  }

  @override
  void didUpdateWidget(BackPressed oldWidget) {
    super.didUpdateWidget(oldWidget);
    oldWidget.emmiter.callback = null;
    _updateWidget();
  }

  /// 添加 back pressed 回调
  void addBackPressedCallback(OnBackPressedCallback callback) {
    _onBackPressedList.add(callback);
  }

  /// 删除 back pressed 回调
  void removeBackPressedCallback(OnBackPressedCallback callback) {
    _onBackPressedList.remove(callback);
  }

  void _updateWidget() {
    widget.emmiter.callback = () async {
      return await _onBackPressed();
    };
  }

  Future<bool> _onBackPressed() async {
    for (var call in _onBackPressedList) {
      if (await call()) {
        return true;
      }
    }
    return false;
  }

  @override
  Widget build(BuildContext context) {
    return widget.child;
  }
}

/// 方便编写代码的 mixin
mixin OnBackPressedMixin<T extends StatefulWidget> on State<T> {

  BackPressedState _pressedState;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_pressedState != null) {
      _pressedState.removeBackPressedCallback(onBackPressed);
    }
    _pressedState = BackPressed.of(context);
    _pressedState.addBackPressedCallback(onBackPressed);
  }

  @override
  void dispose() {
    _pressedState?.removeBackPressedCallback(onBackPressed);
    super.dispose();
  }

  Future<bool> onBackPressed();
}
