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
