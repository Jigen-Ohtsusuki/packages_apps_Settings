/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.settings.deviceinfo.hardwareinfo;

import android.app.ActivityManager;
import android.content.Context;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.slices.Sliceable;

import java.text.DecimalFormat;

public class TotalRAMPreferenceController extends BasePreferenceController {

    public TotalRAMPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(R.bool.config_show_device_model)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean useDynamicSliceSummary() {
        return true;
    }

    @Override
    public boolean isSliceable() {
        return true;
    }

    @Override
    public CharSequence getSummary() {
        ActivityManager actManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        DecimalFormat ramDecimalForm = new DecimalFormat("#.#");
        long totRam = memInfo.totalMem;
        double kb = (double)totRam / 1024.0;
        double mb = (double)totRam / 1048576.0;
        double gb = (double)totRam / 1073741824.0;
        String ramString = "";
        if (gb > 1) {
            ramString = ramDecimalForm.format(gb).concat(" GB");
        } else if (mb > 1) {
            ramString = ramDecimalForm.format(mb).concat(" MB");
        } else {
            ramString = ramDecimalForm.format(kb).concat(" KB");
        }
        return ramString;
    }
}
/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.settings.deviceinfo.hardwareinfo;

import android.content.Context;
import android.text.format.Formatter;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.applications.ProcStatsData;
import com.android.settings.applications.ProcessStatsBase;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.utils.ThreadUtils;

public class TotalRAMPreferenceController extends BasePreferenceController implements
        PreferenceControllerMixin {

    private ProcStatsData mProcStatsData;
    private PreferenceScreen mPreferenceScreen;

    public TotalRAMPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(R.bool.config_show_device_model)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);

        mProcStatsData = getProcStatsData();
        mPreferenceScreen = screen;
        setDuration();
    }

    @Override
    public void updateState(Preference preference) {
        // This is posted on the background thread to speed up fragment launch time for dev options
        // mProcStasData.refreshStats(true) takes ~20ms to run.
        ThreadUtils.postOnBackgroundThread(() -> {
            mProcStatsData.refreshStats(true);
            final ProcStatsData.MemInfo memInfo = mProcStatsData.getMemInfo();
            final String totalResult = Formatter.formatShortFileSize(mContext,
                    (long) memInfo.realTotalRam);
            ThreadUtils.postOnMainThread(
                    () -> mPreferenceScreen.findPreference(mPreferenceKey).setSummary(totalResult));
        });
    }

    @VisibleForTesting
    void setDuration() {
        mProcStatsData.setDuration(ProcessStatsBase.sDurations[0] /* 3 hours */);
    }

    @VisibleForTesting
    ProcStatsData getProcStatsData() {
        return new ProcStatsData(mContext, false);
    }
}
