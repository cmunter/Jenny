package com.midsto.app.jenny.quickstep;

import android.content.Context;
import android.os.Bundle;

import com.midsto.app.jenny.MainThreadExecutor;
import com.midsto.app.jenny.testing.TestInformationHandler;
import com.midsto.app.jenny.testing.TestProtocol;
import com.midsto.app.jenny.uioverrides.states.OverviewState;
import com.midsto.app.jenny.uioverrides.touchcontrollers.PortraitStatesTouchController;
import com.midsto.app.jenny.quickstep.util.LayoutUtils;
import com.midsto.app.jenny.quickstep.views.RecentsView;

import java.util.concurrent.ExecutionException;

public class QuickstepTestInformationHandler extends TestInformationHandler {

    public QuickstepTestInformationHandler(Context context) {
    }

    @Override
    public Bundle call(String method) {
        final Bundle response = new Bundle();
        switch (method) {
            case TestProtocol.REQUEST_HOME_TO_OVERVIEW_SWIPE_HEIGHT: {
                final float swipeHeight =
                        OverviewState.getDefaultSwipeHeight(mDeviceProfile);
                response.putInt(TestProtocol.TEST_INFO_RESPONSE_FIELD, (int) swipeHeight);
                return response;
            }

            case TestProtocol.REQUEST_BACKGROUND_TO_OVERVIEW_SWIPE_HEIGHT: {
                final float swipeHeight =
                        LayoutUtils.getShelfTrackingDistance(mContext, mDeviceProfile);
                response.putInt(TestProtocol.TEST_INFO_RESPONSE_FIELD, (int) swipeHeight);
                return response;
            }

            case TestProtocol.REQUEST_IS_LAUNCHER_INITIALIZED: {
                response.putBoolean(TestProtocol.TEST_INFO_RESPONSE_FIELD,
                        TouchInteractionService.isInitialized());
                return response;
            }

            case TestProtocol.REQUEST_HOTSEAT_TOP: {
                if (mLauncher == null) return null;

                response.putInt(TestProtocol.TEST_INFO_RESPONSE_FIELD,
                        PortraitStatesTouchController.getHotseatTop(mLauncher));
                return response;
            }

            case TestProtocol.REQUEST_OVERVIEW_LEFT_GESTURE_MARGIN: {
                try {
                    final int leftMargin = new MainThreadExecutor().submit(() ->
                            mLauncher.<RecentsView>getOverviewPanel().getLeftGestureMargin()).get();
                    response.putInt(TestProtocol.TEST_INFO_RESPONSE_FIELD, leftMargin);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return response;
            }

            case TestProtocol.REQUEST_OVERVIEW_RIGHT_GESTURE_MARGIN: {
                try {
                    final int rightMargin = new MainThreadExecutor().submit(() ->
                            mLauncher.<RecentsView>getOverviewPanel().getRightGestureMargin()).
                            get();
                    response.putInt(TestProtocol.TEST_INFO_RESPONSE_FIELD, rightMargin);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }

        return super.call(method);
    }
}
