package com.midsto.app.jenny.util;

/**
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.midsto.app.jenny.MainThreadExecutor;

import java.util.function.Consumer;

/**
 * {@link BroadcastReceiver} which watches configuration changes and
 * notifies the callback in case changes which affect the device profile occur.
 */
public class ConfigMonitor extends BroadcastReceiver implements DisplayListener {

    private static final String TAG = "ConfigMonitor";

    private final Point mTmpPoint1 = new Point();
    private final Point mTmpPoint2 = new Point();

    private final Context mContext;
    private final float mFontScale;
    private final int mDensity;

    private final int mDisplayId;
    private final Point mRealSize;
    private final Point mSmallestSize, mLargestSize;

    private Consumer<Context> mCallback;

    public ConfigMonitor(Context context, Consumer<Context> callback) {
        mContext = context;

        Configuration config = context.getResources().getConfiguration();
        mFontScale = config.fontScale;
        mDensity = config.densityDpi;

        Display display = getDefaultDisplay(context);
        mDisplayId = display.getDisplayId();

        mRealSize = new Point();
        display.getRealSize(mRealSize);

        mSmallestSize = new Point();
        mLargestSize = new Point();
        display.getCurrentSizeRange(mSmallestSize, mLargestSize);

        mCallback = callback;

        // Listen for configuration change
        mContext.registerReceiver(this, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));

        // Listen for display manager change
        mContext.getSystemService(DisplayManager.class)
                .registerDisplayListener(this, new Handler(UiThreadHelper.getBackgroundLooper()));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Configuration config = context.getResources().getConfiguration();
        if (mFontScale != config.fontScale || mDensity != config.densityDpi) {
            Log.d(TAG, "Configuration changed.");
            notifyChange();
        }
    }

    @Override
    public void onDisplayAdded(int displayId) { }

    @Override
    public void onDisplayRemoved(int displayId) { }

    @Override
    public void onDisplayChanged(int displayId) {
        if (displayId != mDisplayId) {
            return;
        }
        Display display = getDefaultDisplay(mContext);
        display.getRealSize(mTmpPoint1);

        if (!mRealSize.equals(mTmpPoint1) && !mRealSize.equals(mTmpPoint1.y, mTmpPoint1.x)) {
            Log.d(TAG, String.format("Display size changed from %s to %s", mRealSize, mTmpPoint1));
            notifyChange();
            return;
        }

        display.getCurrentSizeRange(mTmpPoint1, mTmpPoint2);
        if (!mSmallestSize.equals(mTmpPoint1) || !mLargestSize.equals(mTmpPoint2)) {
            Log.d(TAG, String.format("Available size changed from [%s, %s] to [%s, %s]",
                    mSmallestSize, mLargestSize, mTmpPoint1, mTmpPoint2));
            notifyChange();
        }
    }

    private synchronized void notifyChange() {
        if (mCallback != null) {
            Consumer<Context> callback = mCallback;
            mCallback = null;
            new MainThreadExecutor().execute(() -> callback.accept(mContext));
        }
    }

    private Display getDefaultDisplay(Context context) {
        return context.getSystemService(WindowManager.class).getDefaultDisplay();
    }

    public void unregister() {
        try {
            mContext.unregisterReceiver(this);
            mContext.getSystemService(DisplayManager.class).unregisterDisplayListener(this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister config monitor", e);
        }
    }
}
