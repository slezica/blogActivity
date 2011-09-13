package com.slezica.tools.async;

public interface TaskManager {
    public boolean isReady();
    public void runWhenReady(Runnable runnable);
}
