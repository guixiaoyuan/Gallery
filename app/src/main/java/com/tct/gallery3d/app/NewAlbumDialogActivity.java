package com.tct.gallery3d.app;

import android.app.AlertDialog;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.util.GalleryUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewAlbumDialogActivity extends AbstractGalleryActivity {

    private static final String TAG = "NewAlbumDialogActivity";
    private static final String EMPTYSTRING = "";
    private final static SimpleDateFormat SIMPLE_DATE_FOTMAT;
    private static final int MAX_ALBUMNAME_LENGTH = 50;
    private static String SHOW_STATUS = "mShowing";
    public static String KEY_CREATE_NEW_ALBUM = "create-new-album";
    public static String KEY_RENAME_ALBUM = "rename-album";
    public static String KEY_NEED_SELECT_FRAGMENT = "need-select-fragment";
    public static String KEY_OLD_MEDIA_PATH = "old-media-path";
    public static final String AUTHORITY = "media";
    private static final String CONTENT_AUTHORITY_SLASH = "content://" + AUTHORITY + "/";
    private static final String RENAME_QUERY_KEY = "need_update_media_values";
    private boolean mIsNewAlbum = false;
    private boolean mIsRenameAlbum = false;
    private boolean mNeedSelectFragment = false;
    private String mMediaPath;

    static List<Character> sInvalidCharOfFilename;

    private AlertDialog mDialog;
    private String mTitle = EMPTYSTRING;
    private String mPositiveName = EMPTYSTRING;
    private String mNegativeName = EMPTYSTRING;
    private View mEditView = null;
    private EditText mEditText;
    private View mWarningView;
    private TextView mWarningText;
    private Button mPositiveButton;
    private Button mNegativeButton;
    private Context mContext;
    private Thread mRenameThread;

    /* ? : " * <> | \ . */
    static {
        sInvalidCharOfFilename = new ArrayList<Character>();
        sInvalidCharOfFilename.add('*');
        sInvalidCharOfFilename.add('\"');
        sInvalidCharOfFilename.add('/');
        sInvalidCharOfFilename.add('\\');
        sInvalidCharOfFilename.add('?');
        sInvalidCharOfFilename.add('|');
        sInvalidCharOfFilename.add('>');
        sInvalidCharOfFilename.add('<');
        sInvalidCharOfFilename.add(':');
        sInvalidCharOfFilename.add('.');
        SIMPLE_DATE_FOTMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mIsNewAlbum = getIntent().getBooleanExtra(KEY_CREATE_NEW_ALBUM, false);
        mIsRenameAlbum = getIntent().getBooleanExtra(KEY_RENAME_ALBUM, false);
        mNeedSelectFragment = getIntent().getBooleanExtra(KEY_NEED_SELECT_FRAGMENT, false);
        mMediaPath = getIntent().getStringExtra(KEY_OLD_MEDIA_PATH);
        if (mIsNewAlbum) {
            createNewAlbumDialog(mNeedSelectFragment);
        } else if (mIsRenameAlbum) {
            createRenameDialog(mMediaPath);
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        View statusBarBackground = decorView.findViewById(R.id.status_bar_background);
        if (statusBarBackground != null) statusBarBackground.setVisibility(View.GONE);
        View navigationBarBackgroung = decorView.findViewById(R.id.navigation_bar_background);
        if (navigationBarBackgroung != null) navigationBarBackgroung.setVisibility(View.GONE);
    }

    private void createNewAlbumDialog(final boolean needStartNewActivity) {
        final String sdcard0 = Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_PICTURES + "/";
        setTitle(getString(R.string.create_new_album_dialog_title));
        setPositiveName(getString(R.string.create_new_album_confirm));
        setNegativeName(getString(R.string.create_new_album_cancel));
        DialogInterface.OnClickListener onPositiveClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filePath = sdcard0 + getFileName();
                int result = GalleryUtils.createDir(filePath);
                if (result == GalleryUtils.KEY_FILE_CREATE_SUCCESS) {
                    Intent intent = new Intent(NewAlbumDialogActivity.this, NewAlbumSelectActivity.class);
                    if (needStartNewActivity) {
                        intent.putExtra(NewAlbumSelectActivity.TARGET_PATH, filePath);
                        startActivity(intent);
                    } else {
                        intent.putExtra(GalleryConstant.KEY_PATH_RETURN, filePath);
                        setResult(GalleryActivity.RESULT_OK, intent);
                    }
                }
            }
        };
        TextWatcher textWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                File dir = new File(sdcard0 + getFileName());
                if (s.toString().trim().isEmpty()) {
                    setPositiveButtonEnable(false);
                } else {
                    setPositiveButtonEnable(true);
                }
                if (hasInvalidChar(s.toString().trim())) {
                    setWarningEnable(true);
                    setWarningText(getString(R.string.invalid_char_of_albumname));
                } else if (dir.exists() && !s.toString().trim().isEmpty()) {
                    setWarningEnable(true);
                    setWarningText(getString(R.string.album_exists_warning));
                } else {
                    setWarningEnable(false);
                }
            }
        };
        createCustomDialog(onPositiveClickListener, null, textWatcher, null);
    }

    private void createRenameDialog(final String mediapath) {
        if (mediapath == null) return;
        final MediaSet mediaSet = getDataManager().getMediaSet(mMediaPath);
        String oldAlbumName = mediaSet.getName();
        String oldFilePath = mediaSet.getAlbumFilePath();
        String temp = "";
        String[] path = oldFilePath.split(File.separator);
        for (int i = 0; i < path.length - 1; i ++) {
            if (path[i] != null) {
                temp += path[i] + File.separator;
            }
        }
        final String sdcard0 = temp;
        setTitle(getString(R.string.rename_album));
        setPositiveName(getString(R.string.ok));
        setNegativeName(getString(R.string.create_new_album_cancel));
        DialogInterface.OnClickListener onPositiveClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRenameThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String albumName = getFileName();
                        String filePath = sdcard0 + albumName;
                        if (TextUtils.isEmpty(albumName) || albumName == null) {
                            Toast.makeText(mContext, R.string.new_album_empty_warning, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        SharedPreferences sharedPreferences = mContext.getSharedPreferences(GalleryConstant.COLLAPSE_DATA_NAME, Context.MODE_PRIVATE);
                        if (sharedPreferences.getString(mediaSet.getAlbumFilePath(), null) != null) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(mediaSet.getAlbumFilePath(), null);
                            editor.putString(filePath, albumName);
                            editor.commit();
                        }
                        File oldFile = new File(mediaSet.getAlbumFilePath());
                        File newFile = new File(filePath);
                        oldFile.renameTo(newFile);
                        String whereClause = MediaStore.Files.FileColumns.DATA + " = ? ";
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Files.FileColumns.DATA, newFile.getAbsolutePath());
                        ContentProviderClient mediaProvider = mContext.getContentResolver().acquireContentProviderClient(AUTHORITY);
                        Uri uri = Uri.parse(CONTENT_AUTHORITY_SLASH + "external" + "/object");
                        uri = uri.buildUpon().appendQueryParameter(RENAME_QUERY_KEY, "true").build();
                        try {
                            Log.d(TAG, "mediaProvider.update");
                            mediaProvider.update(uri, values, whereClause, new String[]{oldFile.getAbsolutePath()});
                        } catch (RemoteException e) {
                            Log.d(TAG, "RemoteException -----e = " + e);
                            e.printStackTrace();
                        }
                        Log.d(TAG, "scanMedia start");
                        String[] paths = {filePath};
                        MediaScannerConnection.scanFile(mContext, paths, null, null);
                        scanMedia(filePath);
                    }
                });
                mRenameThread.start();
                Log.d(TAG, "scanMedia finish");
                Intent intent = new Intent();
                intent.putExtra(KEY_RENAME_ALBUM, true);
                setResult(RESULT_OK, intent);
                onBackPressed();
            }
        };

        TextWatcher textWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                File dir = new File(sdcard0 + getFileName());
                if (s.toString().trim().isEmpty()) {
                    setPositiveButtonEnable(false);
                } else {
                    setPositiveButtonEnable(true);
                }

                if (hasInvalidChar(s.toString().trim())) {
                    setWarningEnable(true);
                    setWarningText(getString(R.string.invalid_char_of_albumname));
                } else if (dir.exists() && !s.toString().trim().isEmpty()) {
                    setWarningEnable(true);
                    setWarningText(getString(R.string.album_exists_warning));
                } else {
                    setWarningEnable(false);
                }
            }
        };
        createCustomDialog(onPositiveClickListener, null, textWatcher, oldAlbumName);
    }

    public void scanMedia(String targetPath) {
        File target = new File(targetPath);
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(target)));
        if (target.isDirectory()) {
            File[] files = target.listFiles();
            for (File file : files) {
                scanMedia(file.getAbsolutePath());
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRenameThread = null;
    }

    private void setTitle(String title) {
        mTitle = title;
    }

    private void setPositiveName(String positiveName) {
        mPositiveName = positiveName;
    }

    private void setNegativeName(String negativeName) {
        mNegativeName = negativeName;
    }

    private String getFileName() {
        return mEditText.getText().toString().trim();
    }

    private void setPositiveButtonEnable(boolean enable) {
        mPositiveButton.setEnabled(enable);
        if (enable) {
            mPositiveButton.setTextColor(getResources().getColor(R.color.new_album_menu_light));
        } else {
            mPositiveButton.setTextColor(getResources().getColor(R.color.new_album_menu_grey));
        }
    }

    private void setWarningEnable(boolean enable) {
        if (enable) {
            mWarningText.setVisibility(View.VISIBLE);
            mWarningView.setBackgroundResource(R.color.new_album_edit_warning);
            setPositiveButtonEnable(false);
        } else {
            mWarningText.setVisibility(View.INVISIBLE);
            mWarningView.setBackgroundResource(R.color.new_album_edit_line);
        }
    }

    private void setWarningText(String warningText) {
        mWarningText.setText(warningText);
    }

    private void createCustomDialog(DialogInterface.OnClickListener onPositiveClickListener, DialogInterface.OnClickListener onNegativeClickListener,
                                    TextWatcher textWatcher, String oldAlbumName) {
        LayoutInflater inflater = getLayoutInflater();
        mEditView = inflater.inflate(R.layout.dialog_edit, null);
        mEditText = (EditText) mEditView.findViewById(R.id.dialog_edittext);
        mEditText.requestFocus();
        if (oldAlbumName != null) {
            mEditText.setText(oldAlbumName);
            mEditText.setSelection(oldAlbumName.length());
        }
        mWarningView = mEditView.findViewById(R.id.dialog_edittext_bottom_line);
        mWarningText = (TextView) mEditView.findViewById(R.id.dialog_warinning);
        mDialog = new AlertDialog.Builder(this)
                .setTitle(mTitle)
                .setView(mEditView)
                .setPositiveButton(mPositiveName, onPositiveClickListener)
                .setNegativeButton(mNegativeName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create();
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getAction() == KeyEvent.ACTION_UP)) {
                    destroyDialog(dialog);
                }
                return false;
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mEditText.addTextChangedListener(textWatcher);
        mEditText.requestFocus();
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_ALBUMNAME_LENGTH)});
        mDialog.show();

        mPositiveButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        setPositiveButtonEnable(false);
        mNegativeButton = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        mNegativeButton.setTextColor(this.getResources().getColor(R.color.new_album_menu_light));


        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
    }

    private static void destroyDialog(DialogInterface dialog) {
        try {
            if (dialog != null) {
                Field field = dialog.getClass().getSuperclass()
                        .getDeclaredField(SHOW_STATUS);
                if (field != null) {
                    field.setAccessible(true);
                    field.set(dialog, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean hasInvalidChar(String string) {
        StringBuilder invalidCharOfFilename = new StringBuilder();
        boolean hasInvalidChar = false;
        for (char ch : string.toCharArray()) {
            if (sInvalidCharOfFilename.contains(ch)) {
                hasInvalidChar = true;
                invalidCharOfFilename.append(ch);
                invalidCharOfFilename.append(' ');
            }
        }
        return hasInvalidChar;
    }
}
