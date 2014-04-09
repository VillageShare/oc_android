/*
 * VillageShare project
 */
package com.owncloud.android.ui;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.model.OpenGraphAction;
import com.owncloud.android.R;
/**
 * 
 * @author Smruthi Manjunath
 *
 */
public abstract class FriendListView {
    private String text1;
    private String text2;
    
    private int requestCode;
    
    private BaseAdapter adapter;
    
    public FriendListView(String text1, String text2, int requestCode) {
        super();
        this.text1 = text1;
        this.text2 = text2;
        this.requestCode = requestCode;
    }
    
    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
    }
    public String getText1() {
        return text1;
    }

    /**
     * Returns the second row of text.
     *
     * @return the second row of text
     */
    public String getText2() {
        return text2;
    }

    /**
     * Returns the requestCode for starting new Activities.
     *
     * @return the requestCode
     */
    public int getRequestCode() {
        return requestCode;
    }

    /**
     * Sets the first row of text.
     *
     * @param text1 text to set on the first row
     */
    public void setText1(String text1) {
        this.text1 = text1;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Sets the second row of text.
     *
     * @param text2 text to set on the second row
     */
    public void setText2(String text2) {
        this.text2 = text2;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Returns the OnClickListener associated with this list element. To be
     * overridden by the subclasses.
     *
     * @return the OnClickListener associated with this list element
     */
    public abstract View.OnClickListener getOnClickListener();

    /**
     * Populate an OpenGraphAction with the results of this list element.
     *
     * @param action the action to populate with data
     */
    protected abstract void populateOGAction(OpenGraphAction action);

    /**
     * Callback if the OnClickListener happens to launch a new Activity.
     *
     * @param data the data associated with the result
     */
    protected void onActivityResult(Intent data) {}

    /**
     * Save the state of the current element.
     *
     * @param bundle the bundle to save to
     */
    protected void onSaveInstanceState(Bundle bundle) {}

    /**
     * Restore the state from the saved bundle. Returns true if the
     * state was restored.
     *
     * @param savedState the bundle to restore from
     * @return true if state was restored
     */
    protected boolean restoreState(Bundle savedState) {
        return false;
    }

    /**
     * Notifies the associated Adapter that the underlying data has changed,
     * and to re-layout the view.
     */
    protected void notifyDataChanged() {
        adapter.notifyDataSetChanged();
    }

    
}
