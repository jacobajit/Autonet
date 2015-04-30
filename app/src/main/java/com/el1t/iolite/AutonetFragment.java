/*
package com.el1t.iolite;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.el1t.iolite.adapter.SignupListAdapter;
import com.el1t.iolite.item.EighthActivityItem;

import java.util.ArrayList;


public class AutonetFragment extends Fragment
{
    private static final String TAG = "Autonet Fragment";

    private OnFragmentInteractionListener mListener;
    private SignupListAdapter mAdapter;


    public interface OnFragmentInteractionListener {
        public void saveSelection(int BID,String block);

    }

    public AutonetFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_autonet, container, false);

        // Check if list was provided to setup custom ListAdapter
        final Bundle args = getArguments();
        final ArrayList<EighthActivityItem> items;
        if (args != null && (items = args.getParcelableArrayList("list")) != null) {
            Log.d(TAG, "Activity list received");
            mAdapter = new SignupListAdapter(getActivity(), items);
        } else {
            throw new IllegalArgumentException();
        }

        final ListView activityList = (ListView) rootView.findViewById(R.id.activity_list);
        activityList.setAdapter(mAdapter);

        // Submit activity selection on click
        activityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final EighthActivityItem item = (EighthActivityItem) parent.getItemAtPosition(position);
                int aid=findBID(blockName);
                mListener.saveSelection(bid,blockName);
            }
        });



        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStop() {
        super.onPause();
        // This garbage-collects for Android to prevent frame skips
        mAdapter.clear();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.restore();
    }



    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final EighthActivityItem activityItem = mAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.context_select:
                int aid=findBID(blockName);
                mListener.saveSelection(bid,blockName);
                return true;


            default:
                return super.onContextItemSelected(item);
        }
    }

    void setListItems(ArrayList<EighthActivityItem> items) {
        mAdapter.setListItems(items);

    }

    void filter(String query) {
        mAdapter.getFilter().filter(query);
    }
}
*/
