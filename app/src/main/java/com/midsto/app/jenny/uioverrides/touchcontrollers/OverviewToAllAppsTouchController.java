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
package com.midsto.app.jenny.uioverrides.touchcontrollers;

import android.view.MotionEvent;

import com.midsto.app.jenny.AbstractFloatingView;
import com.midsto.app.jenny.Launcher;
import com.midsto.app.jenny.LauncherState;
import com.midsto.app.jenny.Utilities;
import com.midsto.app.jenny.userevent.nano.LauncherLogProto;
import com.midsto.app.jenny.quickstep.TouchInteractionService;
import com.midsto.app.jenny.quickstep.views.RecentsView;

import static com.midsto.app.jenny.LauncherState.ALL_APPS;
import static com.midsto.app.jenny.LauncherState.NORMAL;
import static com.midsto.app.jenny.LauncherState.OVERVIEW;

/**
 * Touch controller from going from OVERVIEW to ALL_APPS.
 *
 * This is used in landscape mode. It is also used in portrait mode for the fallback recents.
 */
public class OverviewToAllAppsTouchController extends PortraitStatesTouchController {

    public OverviewToAllAppsTouchController(Launcher l) {
        super(l, true /* allowDragToOverview */);
    }

    @Override
    protected boolean canInterceptTouch(MotionEvent ev) {
        if (mCurrentAnimation != null) {
            // If we are already animating from a previous state, we can intercept.
            return true;
        }
        if (AbstractFloatingView.getTopOpenView(mLauncher) != null) {
            return false;
        }
        if (mLauncher.isInState(ALL_APPS)) {
            // In all-apps only listen if the container cannot scroll itself
            return mLauncher.getAppsView().shouldContainerScroll(ev);
        } else if (mLauncher.isInState(NORMAL)) {
            return (ev.getEdgeFlags() & Utilities.EDGE_NAV_BAR) == 0;
        } else if (mLauncher.isInState(OVERVIEW)) {
            RecentsView rv = mLauncher.getOverviewPanel();
            return ev.getY() > (rv.getBottom() - rv.getPaddingBottom());
        } else {
            return false;
        }
    }

    @Override
    protected LauncherState getTargetState(LauncherState fromState, boolean isDragTowardPositive) {
        if (fromState == ALL_APPS && !isDragTowardPositive) {
            // Should swipe down go to OVERVIEW instead?
            return TouchInteractionService.isConnected() ?
                    mLauncher.getStateManager().getLastState() : NORMAL;
        } else if (isDragTowardPositive) {
            return ALL_APPS;
        }
        return fromState;
    }

    @Override
    protected int getLogContainerTypeForNormalState() {
        return LauncherLogProto.ContainerType.WORKSPACE;
    }
}
