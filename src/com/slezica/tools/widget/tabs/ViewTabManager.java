package com.slezica.tools.widget.tabs;

import android.view.View;
import android.view.ViewGroup;


public class ViewTabManager extends TabManager<View, ViewGroup> {

    public ViewTabManager(ViewGroup container) {
        super(container);
    }

    @Override
    protected void performSwitch(int id) {
        if (id == getCurrentTabId())
            return;
        
        View view = getTabObject(id);
        ViewGroup container = getContainer();
        
        container.removeView(getCurrentTab());
        container.addView(view);
    }

    @Override
    protected void onContainerChanged(ViewGroup prevContainer, ViewGroup newContainer) {
        View view = getCurrentTab();
        
        prevContainer.removeView(view);
        newContainer.addView(view);
    }

}
