package ru.alexbykov.nopermission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;

/**
 * Date: 30.07.2017
 * Time: 18:39
 * Project: NoPermission
 *
 * @author Mike Antipiev @nindzyago
 * @author Alex Bykov @NoNews
 */
public class PermissionHelper {


    private static final int PERMISSION_REQUEST_CODE = 1005001;
    private Activity activity;
    private String[] permissions;
    private Runnable successListener;
    private Runnable failureListener;
    private Runnable neverAskAgainListener;

    public PermissionHelper(Activity activity) {
        permissions = new String[1];
        this.activity = activity;
    }

    public PermissionHelper check(String permission) {
        this.permissions[0] = permission;
        return this;
    }

    public PermissionHelper check(String... permissions) {
        this.permissions = permissions;
        return this;
    }

    public PermissionHelper onSuccess(Runnable listener) {
        this.successListener = listener;
        return this;
    }

    public PermissionHelper onFailure(Runnable listener) {
        this.failureListener = listener;
        return this;
    }


    public PermissionHelper onNeverAskAgain(Runnable listener) {
        this.neverAskAgainListener = listener;
        return this;
    }

    public void run() {
        if (isNeedAskPermission()) {

            if (successListener != null && failureListener != null && neverAskAgainListener != null) {
                activity.requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            } else
                throw new RuntimeException("OnPermissionSuccessListener or OnPermissionFailureListener not implemented. Use methods: onSuccess and onFailure");
        } else if(successListener != null) {
            successListener.run();
        }
    }

    private boolean isNeedAskPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (String permission : permissions) {
                if (isPermissionNotGranted(permission)) {
                    if (isNeedAskPermission()) {

                        if (isNewerAskAgain(permission)) {
                            neverAskAgainListener.run();
                        } else {
                            failureListener.run();
                        }
                        return;
                    }
                }
            }
        }
        successListener.run();
    }

    private boolean isPermissionNotGranted(String permission) {
        return ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isNewerAskAgain(String permission) {
        return !activity.shouldShowRequestPermissionRationale(permission);
    }


    public void unsubscribe() {
        if (failureListener != null) {
            failureListener = null;
        }
        if (successListener != null) {
            successListener = null;
        }
        if (neverAskAgainListener != null) {
            neverAskAgainListener = null;
        }
    }
}
