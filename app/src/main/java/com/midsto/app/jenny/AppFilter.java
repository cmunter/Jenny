package com.midsto.app.jenny;

import android.content.ComponentName;
import android.content.Context;

import com.midsto.app.jenny.util.ResourceBasedOverride;

public class AppFilter implements ResourceBasedOverride {

    public static AppFilter newInstance(Context context) {
        return Overrides.getObject(AppFilter.class, context, R.string.app_filter_class);
    }

    public boolean shouldShowApp(ComponentName app) {
        return true;
    }
}
