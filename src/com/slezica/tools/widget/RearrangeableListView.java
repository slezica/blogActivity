package com.etermax.smandroid.widget;

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

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class RearrangeableListView extends ListView {

    public static interface RearrangeListener {
        void onGrab(int index);
        boolean onRearrangeRequested(int fromIndex, int toIndex);
        void onDrop();
    }
    
    /* To avoid burdening the ListView with data management logic,
     * the user must provide a RearrangeListener that will:
     * 
     * a) Notified when an item is grabbed.
     * 
     * b) Notified when an item is supposed to be rearranged,
     *    and given the change to allow or disallow the movement
     *    by returning true/false
     *    
     * c) Notified when the grabbed item is dropped.
     * 
     * EXAMPLE IMPLEMENTATION (WITH AN ARRAY ADAPTER IN MIND):
     * 
     *  final RearrangeListener mRearrangeListener = new RearrangeListener () {
     *      @Override
     *       public void onGrab(int index) {
     *           getItem(index).doSomething();
     *           notifyDataSetChanged();
     *       }
     *       
     *       public boolean onRearrangeRequested(int fromIndex, int toIndex) {
     *           
     *           if (toIndex >= 0 && toIndex < getCount()) {
     *               Object item = getItem(fromIndex);
     *               
     *                  remove(item);
     *                  insert(item, toIndex);
     *                  notifyDataSetChanged();
     *               
     *                  return true;
     *              }
     *           
     *              return false;
     *          }
     *
     *          @Override
     *          public void onDrop() {
     *              doSomethingElse();
     *              notifyDataSetChanged();
     *          }
     *      };
     *  }
     */
    
    public interface MovableView {
        public boolean onGrabAttempt(int x, int y);
        public void onRelease();
    }
    
    /* Some Views may want to define areas from which they can be
     * dragged. That logic doesn't concern us either. Views in this
     * list that implement MovableView will be:
     * 
     * a) Notified upon a grab attempt, and asked if they'll allow
     *    it given the touch coordinates (x, y) (relative to their own
     *    top-left).
     *    
     * b) Notified when they are released.
     * 
     * EXAMPLE IMPLEMENTATION (View with a grab-handle in mind):
     * 
     *  @Override
     *  public boolean onGrabAttempt(int x, int y) {
     *      Rect hitRect = new Rect();
     *      this.mMyHandleView.getHitRect(hitRect);
     *      
     *      return hitRect.contains(x, y);
     *  }
     *
     *  @Override
     *  public void onRelease() {}
     *  
     */
    
    private static final int AUTO_SCROLL_DELAY = 300,
            
                             AUTO_SCROLL_UP       = -1,
                             AUTO_SCROLL_DOWN     =  1,
                             AUTO_SCROLL_DISABLED =  0;
    
    private int mHeldItemIndex = -1;
    private boolean mRearrangeEnabled = false;
    
    private RearrangeListener mListener;
    
    private int mScrollState = AUTO_SCROLL_DISABLED;
    
    public RearrangeableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RearrangeableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RearrangeableListView(Context context) {
        super(context);
    }
    
    public void setRearrangeListener(RearrangeListener listener) {
        mListener = listener;
    }
    
    public RearrangeListener getRearrangeListener() {
        return mListener;
    }

    public void setRearrangeEnabled(boolean value) {
        mRearrangeEnabled = value;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /* We want to steal the DOWN event from the list items */
        
        if (mListener != null && mRearrangeEnabled) {
            int action = (ev.getAction() & MotionEvent.ACTION_MASK);
            
            if (action == MotionEvent.ACTION_DOWN) {
            
                int x = (int) ev.getX(),
                    y = (int) ev.getY();
                
                return grabItemAt(x, y);
            }
        }
        
        return super.onInterceptTouchEvent(ev);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mListener == null || !mRearrangeEnabled || mHeldItemIndex < 0)
            return super.onTouchEvent(ev);
        
        int action = (ev.getAction() & MotionEvent.ACTION_MASK);
        
        switch(action) {
        
            case MotionEvent.ACTION_MOVE: {
                
                int x = (int) ev.getX(),
                    y = (int) ev.getY();
                
                int otherIndex = itemIndexAt(x, y);
                
                if (otherIndex < 0)
                    return true;
                
                View view = getChildAt(mHeldItemIndex - getFirstVisiblePosition());
                
                if (otherIndex != mHeldItemIndex) {
                    moveHeldItem(otherIndex);

                } else {
                    /* We may be at the top, or the bottom, and need to scroll
                    
                     * Note that, while we'll check if this item is the first or last
                     * one visible, we won't verify if it's the first or last in our
                     * backing dataset. We leave that task to our listener, since
                     * it may want to create additional items and insert them while
                     * rearranging.
                     */
                    if (mHeldItemIndex == getFirstVisiblePosition()
                        && y < view.getHeight() / 2) {
                        
                        /* The user wants to rearrange the item upwards, but this is
                         * the first item visible to him. If the move is approved by our
                         * listener, we'll need to scroll up.
                         */
                        setAutoScroll(AUTO_SCROLL_UP);
                        
                    } else
                    if (mHeldItemIndex == getLastVisiblePosition()
                        && y > getHeight() - view.getHeight() / 2) {
                    
                        /* Same logic as above, only, you know, down */
                        setAutoScroll(AUTO_SCROLL_DOWN);
                        
                    } else {
                        /* We are at the top/bottom, but the user doesn't seem to
                         * want to keep dragging the view upwards/downwards.
                         */
                        setAutoScroll(AUTO_SCROLL_DISABLED);
                    }
                }
                
            } break;
            
            case MotionEvent.ACTION_UP: {
                
                dropHeldItem();
                setAutoScroll(AUTO_SCROLL_DISABLED);
                
            } break;
        
        }
        
        return true;
    }
    
    private boolean grabItemAt(int x, int y) {
        /* We'll return whether we successfully grabbed the item */
        
        int itemIndex = itemIndexAt(x, y);
        
        if (itemIndex >= 0) {
            View itemView = getChildAt(itemIndex - getFirstVisiblePosition());
            
            if (itemView instanceof MovableView) {
                /* Views implementing this interface can
                 * catch this event and decide whether they'll
                 * allow us to move them.
                 */
                
                MovableView movableView = (MovableView) itemView;
                
                int childX = x - itemView.getLeft(),
                    childY = y - itemView.getTop();
                
                boolean allowed = movableView.onGrabAttempt(childX, childY);
                
                if (allowed) {
                    /* View says yes! Yee-ha!*/
                    grabItemByIndex(itemIndex);
                }
                
                System.out.println("Allowed: " + allowed);
                return allowed;
                
            } else {
                /* Ironically, views that don't implement MovableView are
                 * assumed to be ok with this.
                */
                
                grabItemByIndex(itemIndex);
                System.out.println("Allowed: true");
                return true;
            }
        }
        
        System.out.println("Allowed: false");
        return false; /* itemIndexAt returned invalid index */
    }
    
    private void grabItemByIndex(int itemIndex) {
        /* No validations are made. We assume the index is correct */
        mHeldItemIndex = itemIndex;
        mListener.onGrab(itemIndex);
    }
    
    private boolean moveHeldItem(int toIndex) {
        boolean allowed = mListener.onRearrangeRequested(mHeldItemIndex, toIndex);
        
        if (allowed) {
            /* The rearrangement took place, according to the listener */
            mHeldItemIndex = toIndex;
        }
        
        return allowed;
    }
    
    private void dropHeldItem() {
        View itemView = getChildAt(mHeldItemIndex - getFirstVisiblePosition());

        if (itemView instanceof MovableView) {
            ((MovableView) itemView).onRelease();
        }

        mListener.onDrop();
        mHeldItemIndex = -1;
    }
    
    /* We'll use this Rect to avoid creating a new one in each call to itemIndexAt() */
    final Rect itemIndexAt_tempRect = new Rect();
    
    private int itemIndexAt(int x, int y) {
        Rect hitRect = itemIndexAt_tempRect;
        
        int childCount = getChildCount();
        
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).getHitRect(hitRect);
            
            if (hitRect.contains(x, y))
                return i + getFirstVisiblePosition();
        }
        
        return -1;
    }
    
    
    final Runnable mAutoScroll = new Runnable() {
        @Override
        public void run() {
            
            if ((mScrollState != AUTO_SCROLL_DISABLED)
                && moveHeldItem(mHeldItemIndex + mScrollState)) {
                
                if (mScrollState == AUTO_SCROLL_UP)
                    setSelection(getFirstVisiblePosition() - 1);
                else
                    setSelection(getFirstVisiblePosition() + 1);
                
                postDelayed(this, AUTO_SCROLL_DELAY);
            }
        }
    };
    
    private void setAutoScroll(int scrollState) {
        
        if (mScrollState == scrollState)
            return; /* Nothing to do */
        
        if (mScrollState == AUTO_SCROLL_DISABLED) {
            /* We have to start scrolling */
            mScrollState = scrollState;
            mAutoScroll.run();
            
        } else {
            /* We were already scrolling */
            mScrollState = scrollState;
            
            if (mScrollState == AUTO_SCROLL_DISABLED)
                removeCallbacks(mAutoScroll);
        }
        
    }
}
