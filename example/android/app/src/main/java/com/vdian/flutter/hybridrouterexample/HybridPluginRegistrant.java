package com.vdian.flutter.hybridrouterexample;


import io.flutter.plugin.common.PluginRegistry;

public final class HybridPluginRegistrant {
  public static void registerWith(PluginRegistry registry) {
    if (alreadyRegisteredWith(registry)) {
      return;
    }
    HybridPlugin.registerWith(registry.registrarFor("com.vdian.flutter.hybridrouterexample.HybridPlugin"));
  }

  private static boolean alreadyRegisteredWith(PluginRegistry registry) {
    final String key = HybridPluginRegistrant.class.getCanonicalName();
    if (registry.hasPlugin(key)) {
      return true;
    }
    registry.registrarFor(key);
    return false;
  }
}
