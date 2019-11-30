/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.midsto.app.jenny.model;

import com.midsto.app.jenny.AllAppsList;
import com.midsto.app.jenny.LauncherAppState;
import com.midsto.app.jenny.LauncherModel.Callbacks;
import com.midsto.app.jenny.util.ComponentKey;
import com.midsto.app.jenny.widget.WidgetListRowEntry;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class LoaderResults extends BaseLoaderResults {

    public LoaderResults(LauncherAppState app, BgDataModel dataModel,
            AllAppsList allAppsList, int pageToBindFirst, WeakReference<Callbacks> callbacks) {
        super(app, dataModel, allAppsList, pageToBindFirst, callbacks);
    }

    @Override
    public void bindDeepShortcuts() {
        final HashMap<ComponentKey, Integer> shortcutMapCopy;
        synchronized (mBgDataModel) {
            shortcutMapCopy = new HashMap<>(mBgDataModel.deepShortcutMap);
        }
        executeCallbacksTask(c -> c.bindDeepShortcutMap(shortcutMapCopy), mUiExecutor);
    }

    @Override
    public void bindWidgets() {
        final ArrayList<WidgetListRowEntry> widgets =
                mBgDataModel.widgetsModel.getWidgetsList(mApp.getContext());
        executeCallbacksTask(c -> c.bindAllWidgets(widgets), mUiExecutor);
    }
}
