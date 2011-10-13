package com.etermax.smandroid.widget.tab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;


public class FragmentTabManager extends TabManager<Fragment, Integer> {

    /* The container type we'll use is the target ViewGroup's id */
    
    private FragmentManager mManager;
    
    public FragmentTabManager(FragmentManager manager, int containerId) {
        super(containerId);
        mManager = manager;
    }
    
    @Override
    public void onContainerChanged(Integer prevContainerId, Integer newContainerId) {
        /* If we already had a visible fragment in a previous container,
         * we have to move it over to the new one.
         */
        Fragment fragment = getCurrentTab();
        
        mManager.beginTransaction()
            .remove(fragment)
            .add(newContainerId, fragment)
        .commit();
    }
    
    @Override
    public void performSwitch(int id) {
        if (id == getCurrentTabId())
            return;
            
        Fragment fragment = getTabObject(id);
        
        mManager.beginTransaction()
            .replace(getContainer(), fragment)
        .commit();
    }
}
