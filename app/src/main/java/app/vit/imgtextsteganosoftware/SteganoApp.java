package app.vit.imgtextsteganosoftware;

import android.app.Application;

import app.vit.imgtextsteganosoftware.utils.ThemeHelper;

public class SteganoApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    ThemeHelper.applySavedMode(this);
  }
}
