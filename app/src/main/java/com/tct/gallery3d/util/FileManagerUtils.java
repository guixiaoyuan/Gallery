package com.tct.gallery3d.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

//[FEATURE]-Add-BEGIN by jian.pan1,11/05/2014, For FR824779 Video subtitle
public class FileManagerUtils {

    /*
     * Support file manager's package name array. You can add other package name
     * if you want to support it. And first one will be set default.
     */
    private static final String[] FILE_MANAGER_ARRAY = new String[] {
          // [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014, For FR824779 Video subtitle
            "com.jrdcom.filemanager",
          // [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014, For FR824779 Video subtitle
            "com.tct.filemanager",
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-28,FR824779 begin
            // add other support file manager
            "com.mediatek.filemanager", // for Diablo X+,EOS 3G kk
            "com.tcl.filemanager",// for Hero8
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-28,FR824779 end
            "com.speedsoftware.rootexplorer" };

    /*
     *  Default support file manager name.
     */
    private static final String DEFAULT_SUPPORT_FILE_MANAGER = FILE_MANAGER_ARRAY[0];

    private ArrayList<String> mPackageNameList = new ArrayList<String>();

    private Context mContext;

    public FileManagerUtils(Context context) {
        super();
        this.mContext = context;
        initPkgNameList();
    }

    private void initPkgNameList() {
        if (mPackageNameList != null) {
            mPackageNameList.clear();
            for (String str : FILE_MANAGER_ARRAY) {
                mPackageNameList.add(str);
            }
        }
    }

    /**
     * get support file manager application.
     *
     * @return support file manager package names
     */
    public String getSupportFileManager() {
        String fileManagerPkgName = DEFAULT_SUPPORT_FILE_MANAGER;
        PackageManager pm = mContext.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ApplicationInfo> applicationList = pm
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo appInfo : applicationList) {
            String pkgName = appInfo.packageName;
            if (mPackageNameList.contains(pkgName)) {
                fileManagerPkgName = pkgName;
            }
        }
        return fileManagerPkgName;
    }

}
//[FEATURE]-Add-END by jian.pan1
