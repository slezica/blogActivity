package com.slezica.tools.widget.tabs;

import java.util.HashMap;
import java.util.Map;


public abstract class TabManager<TabType, ContainerType> {
    
    private int mCurrentTab;
    private Map<Integer, TabType> mTabs = new HashMap<Integer, TabType>();
    
    private ContainerType mContainer;
    
    public TabManager(ContainerType container) {
        mContainer = container;
    }
    
    public void setContainer(ContainerType container) {
        if (mContainer != null && mContainer != container)
            onContainerChanged(mContainer, container);
        
        mContainer = container;
    }
    
    public ContainerType getContainer() {
        return mContainer;
    }
    
    public void addTab(int id, TabType tabObject) {
        mTabs.put(id, tabObject);
    }

    public void switchTo(int id) {
        if (!mTabs.containsKey(id))
            throw new IllegalArgumentException("No tab with id " + id + " found.");
        
        performSwitch(id);
        mCurrentTab = id;
    }
    
    protected abstract void performSwitch(int id);
    protected abstract void onContainerChanged(ContainerType prevContainer, ContainerType newContainer);
    
    public int getCurrentTabId() {
        return mCurrentTab;
    }
    
    public TabType getCurrentTab() {
        return getTabObject(getCurrentTabId());
    }
    
    public TabType getTabObject(int id) {
        return mTabs.get(id);
    }
    
    @SuppressWarnings("unchecked")
    public TabType[] getTabs() {
        return (TabType[]) mTabs.values().toArray();
    }
}
