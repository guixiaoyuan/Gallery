package com.tct.gallery3d.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.fragment.PhotoFragment;

public abstract class AbstractGalleryFragment extends Fragment {

    public static final String TAG = "AbstractGalleryFragment";
    private ProgressDialog mDataLoaderDialog = null;

    protected abstract void onActionResult(int requestCode, int resultCode, Intent data);

    public void showProgressDialog() {
        if (mDataLoaderDialog == null) {
            mDataLoaderDialog = new ProgressDialog(getActivity());
            mDataLoaderDialog.setMessage(getString(R.string.loading_data_message));
            mDataLoaderDialog.setCanceledOnTouchOutside(false);
        }
        mDataLoaderDialog.show();
    }

    public void dismissProgressDialog() {
        if (mDataLoaderDialog != null) {
            mDataLoaderDialog.dismiss();
        }
        mDataLoaderDialog = null;
    }

    public GalleryContext getGalleryContext() {
        return (GalleryContext) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle paramBundle) {
        super.onActivityCreated(paramBundle);
    }

    public abstract boolean onBackPressed();

    public static boolean checkClickable(AbstractGalleryActivity context) {
        AbstractGalleryFragment content = context.getContent();
        if (content != null && content instanceof PhotoFragment) {
            return false;
        }
        return true;
    }

}
