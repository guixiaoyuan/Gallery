package com.tct.gallery3d.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.AbstractGalleryFragment;
import com.tct.gallery3d.app.adapter.CollapseManageSetDataAdapter;
import com.tct.gallery3d.data.AlbumSetManager;


public class CollapseManageFragment extends AbstractGalleryFragment {

    protected RelativeLayout mNoContentView;
    private AbstractGalleryActivity mContext;
    private ListView mListView;
    private static final int NO_ALBUM_COUNT = 0;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (AbstractGalleryActivity) getGalleryContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.collapse_manage_page_layout, container, false);
        mNoContentView = (RelativeLayout) v.findViewById(R.id.no_content_album_list); // MODIFIED by Yaoyu.Yang, 2016-08-18,BUG-2208330
        if (AlbumSetManager.getInstance().getAllALbum().size() == NO_ALBUM_COUNT){
            mNoContentView.setVisibility(View.VISIBLE);
        }else {
            mNoContentView.setVisibility(View.INVISIBLE);
        }
        mListView = (ListView) v.findViewById(R.id.collapse_recycler_view);
        mListView.setAdapter(new CollapseManageSetDataAdapter(mContext));
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.getDataManager().notifyPrivateMode();
    }
    @Override
    protected void onActionResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
