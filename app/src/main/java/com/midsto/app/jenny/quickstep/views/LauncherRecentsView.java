/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.midsto.app.jenny.quickstep.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.midsto.app.jenny.DeviceProfile;
import com.midsto.app.jenny.Hotseat;
import com.midsto.app.jenny.Launcher;
import com.midsto.app.jenny.LauncherState;
import com.midsto.app.jenny.LauncherStateManager.StateListener;
import com.midsto.app.jenny.R;
import com.midsto.app.jenny.anim.Interpolators;
import com.midsto.app.jenny.appprediction.PredictionUiStateManager;
import com.midsto.app.jenny.appprediction.PredictionUiStateManager.Client;
import com.midsto.app.jenny.views.ScrimView;
import com.midsto.app.jenny.quickstep.SysUINavigationMode;
import com.midsto.app.jenny.quickstep.util.ClipAnimationHelper;
import com.midsto.app.jenny.quickstep.util.ClipAnimationHelper.TransformParams;
import com.midsto.app.jenny.quickstep.util.LayoutUtils;

import static com.midsto.app.jenny.LauncherState.ALL_APPS;
import static com.midsto.app.jenny.LauncherState.ALL_APPS_HEADER_EXTRA;
import static com.midsto.app.jenny.LauncherState.NORMAL;
import static com.midsto.app.jenny.LauncherState.OVERVIEW;
import static com.midsto.app.jenny.LauncherState.RECENTS_CLEAR_ALL_BUTTON;
import static com.midsto.app.jenny.LauncherState.SPRING_LOADED;
import static com.midsto.app.jenny.QuickstepAppTransitionManagerImpl.ALL_APPS_PROGRESS_OFF_SCREEN;
import static com.midsto.app.jenny.allapps.AllAppsTransitionController.ALL_APPS_PROGRESS;
import static com.midsto.app.jenny.config.FeatureFlags.ENABLE_QUICKSTEP_LIVE_TILE;

/**
 * {@link RecentsView} used in Launcher activity
 */
@TargetApi(Build.VERSION_CODES.O)
public class LauncherRecentsView extends RecentsView<Launcher> implements StateListener {

    private final TransformParams mTransformParams = new TransformParams();

    public LauncherRecentsView(Context context) {
        this(context, null);
    }

    public LauncherRecentsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LauncherRecentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setContentAlpha(0);
        mActivity.getStateManager().addStateListener(this);
    }

    @Override
    public void startHome() {
        mActivity.getStateManager().goToState(NORMAL);
    }

    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        if (BaseFlags.ENABLE_QUICKSTEP_LIVE_TILE.get()) {
            LauncherState state = mActivity.getStateManager().getState();
            if (state == OVERVIEW || state == ALL_APPS) {
                redrawLiveTile(false);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        maybeDrawEmptyMessage(canvas);
        super.draw(canvas);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        updateEmptyMessage();
    }

    @Override
    protected void onTaskStackUpdated() {
        // Lazily update the empty message only when the task stack is reapplied
        updateEmptyMessage();
    }

    /**
     * Animates adjacent tasks and translate hotseat off screen as well.
     */
    @Override
    public AnimatorSet createAdjacentPageAnimForTaskLaunch(TaskView tv,
                                                           ClipAnimationHelper helper) {
        AnimatorSet anim = super.createAdjacentPageAnimForTaskLaunch(tv, helper);

        if (!SysUINavigationMode.getMode(mActivity).hasGestures) {
            // Hotseat doesn't move when opening recents with the button,
            // so don't animate it here either.
            return anim;
        }

        float allAppsProgressOffscreen = ALL_APPS_PROGRESS_OFF_SCREEN;
        LauncherState state = mActivity.getStateManager().getState();
        if ((state.getVisibleElements(mActivity) & ALL_APPS_HEADER_EXTRA) != 0) {
            float maxShiftRange = mActivity.getDeviceProfile().heightPx;
            float currShiftRange = mActivity.getAllAppsController().getShiftRange();
            allAppsProgressOffscreen = 1f + (maxShiftRange - currShiftRange) / maxShiftRange;
        }
        anim.play(ObjectAnimator.ofFloat(
                mActivity.getAllAppsController(), ALL_APPS_PROGRESS, allAppsProgressOffscreen));

        ObjectAnimator dragHandleAnim = ObjectAnimator.ofInt(
                mActivity.findViewById(R.id.scrim_view), ScrimView.DRAG_HANDLE_ALPHA, 0);
        dragHandleAnim.setInterpolator(Interpolators.ACCEL_2);
        anim.play(dragHandleAnim);

        return anim;
    }

    @Override
    protected void getTaskSize(DeviceProfile dp, Rect outRect) {
        LayoutUtils.calculateLauncherTaskSize(getContext(), dp, outRect);
    }

    @Override
    protected void onTaskLaunchAnimationUpdate(float progress, TaskView tv) {
        if (BaseFlags.ENABLE_QUICKSTEP_LIVE_TILE.get()) {
            if (mRecentsAnimationWrapper.targetSet != null && tv.isRunningTask()) {
                mTransformParams.setProgress(1 - progress)
                        .setSyncTransactionApplier(mSyncTransactionApplier)
                        .setForLiveTile(true);
                mClipAnimationHelper.applyTransform(mRecentsAnimationWrapper.targetSet,
                        mTransformParams);
            } else {
                redrawLiveTile(true);
            }
        }
    }

    @Override
    protected void onTaskLaunched(boolean success) {
        if (success) {
            mActivity.getStateManager().goToState(NORMAL, false /* animate */);
        } else {
            LauncherState state = mActivity.getStateManager().getState();
            mActivity.getAllAppsController().setState(state);
        }
        super.onTaskLaunched(success);
    }

    @Override
    public boolean shouldUseMultiWindowTaskSizeStrategy() {
        return mActivity.isInMultiWindowMode();
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        if (BaseFlags.ENABLE_QUICKSTEP_LIVE_TILE.get() && mEnableDrawingLiveTile) {
            redrawLiveTile(true);
        }
    }

    @Override
    public void redrawLiveTile(boolean mightNeedToRefill) {
        if (!mEnableDrawingLiveTile || mRecentsAnimationWrapper == null
                || mClipAnimationHelper == null) {
            return;
        }
        TaskView taskView = getRunningTaskView();
        if (taskView != null) {
            taskView.getThumbnail().getGlobalVisibleRect(mTempRect);
            int offsetX = (int) (mTaskWidth * taskView.getScaleX() * getScaleX()
                    - mTempRect.width());
            int offsetY = (int) (mTaskHeight * taskView.getScaleY() * getScaleY()
                    - mTempRect.height());
            if (((mCurrentPage != 0) || mightNeedToRefill) && offsetX > 0) {
                if (mTempRect.left - offsetX < 0) {
                    mTempRect.left -= offsetX;
                } else {
                    mTempRect.right += offsetX;
                }
            }
            if (mightNeedToRefill && offsetY > 0) {
                mTempRect.top -= offsetY;
            }
            mTempRectF.set(mTempRect);
            mTransformParams.setProgress(1f)
                    .setCurrentRectAndTargetAlpha(mTempRectF, taskView.getAlpha())
                    .setSyncTransactionApplier(mSyncTransactionApplier);
            if (mRecentsAnimationWrapper.targetSet != null) {
                mClipAnimationHelper.applyTransform(mRecentsAnimationWrapper.targetSet,
                        mTransformParams);
            }
        }
    }

    @Override
    public void reset() {
        super.reset();

        // We are moving to home or some other UI with no recents. Switch back to the home client,
        // the home predictions should have been updated when the activity was resumed.
        PredictionUiStateManager.INSTANCE.get(getContext()).switchClient(Client.HOME);
    }

    @Override
    public void onStateTransitionStart(LauncherState toState) {
        setOverviewStateEnabled(toState.overviewUi);
        setFreezeViewVisibility(true);
    }

    @Override
    public void onStateTransitionComplete(LauncherState finalState) {
        if (finalState == NORMAL || finalState == SPRING_LOADED) {
            // Clean-up logic that occurs when recents is no longer in use/visible.
            reset();
        }
        setOverlayEnabled(finalState == OVERVIEW);
        setFreezeViewVisibility(false);
    }

    @Override
    public void setOverviewStateEnabled(boolean enabled) {
        super.setOverviewStateEnabled(enabled);
        if (enabled) {
            LauncherState state = mActivity.getStateManager().getState();
            boolean hasClearAllButton = (state.getVisibleElements(mActivity)
                    & RECENTS_CLEAR_ALL_BUTTON) != 0;
            setDisallowScrollToClearAll(!hasClearAllButton);
        }
    }

    @Override
    protected boolean shouldStealTouchFromSiblingsBelow(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // Allow touches to go through to the hotseat.
            Hotseat hotseat = mActivity.getHotseat();
            boolean touchingHotseat = hotseat.isShown()
                    && mActivity.getDragLayer().isEventOverView(hotseat, ev, this);
            return !touchingHotseat;
        }
        return super.shouldStealTouchFromSiblingsBelow(ev);
    }
}
