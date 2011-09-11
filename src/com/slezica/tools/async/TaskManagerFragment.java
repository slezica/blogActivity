/*
 * Copyright (C) 2011 Santiago Lezica
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

package com.slezica.tools.async;

import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class TaskManagerFragment extends Fragment {

    public static final String DEFAULT_TAG = "TaskManagerFragment";

    protected final Object mLock = new Object();

    protected Boolean mReady = true;
    protected List<Runnable> mPendingCallbacks = new LinkedList<Runnable>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        synchronized (mLock) {
            mReady = false;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        synchronized (mLock) {
            mReady = true;

            int pendingCallbacks = mPendingCallbacks.size();

            while (pendingCallbacks-- > 0)
                runNow(mPendingCallbacks.remove(0));
        }
    }

    public boolean isReady() {
        synchronized (mLock) {
            return mReady;
        }
    }

    protected void setReady(boolean ready) {
        synchronized (mLock) {
            mReady = ready;
        }
    }

    protected void addPending(Runnable runnable) {
        synchronized (mLock) {
            mPendingCallbacks.add(runnable);
        }
    }

    public void runWhenReady(Runnable runnable) {
        if (isReady())
            runNow(runnable);

        else
            addPending(runnable);
    }

    protected void runNow(Runnable runnable) {
        getActivity().runOnUiThread(runnable);
    }
}
