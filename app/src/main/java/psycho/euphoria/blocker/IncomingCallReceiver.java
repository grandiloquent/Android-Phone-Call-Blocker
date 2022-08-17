package psycho.euphoria.blocker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.android.internal.telephony.ITelephony;

public class IncomingCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean blocked = preferences
                .getBoolean("key_block_non_phone", true);
        String numbers =preferences.getString("key_numbers","");
        try {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                try {
                    if (number != null) {
                        if (blocked && number.length() != 11)
                            stopCall(context);
                        if (Arrays.stream(numbers.split("\n")).anyMatch(x->x.trim().equals(number)))
                            stopCall(context);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("B5aOx2", String.format("onReceive,  >-----%s", e.getMessage()));
                }


            }
//            if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)){
//            }
//            if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)){
//            }
        } catch (Exception e) {
            Log.e("B5aOx2", String.format("onReceive, %s", e.getMessage()));

        }
    }

    public static boolean stopCall(Context context) {
        if (context == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 28) { //android 9及以上商用此方式挂断电话
            try {
                if (context.checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                    TelecomManager manager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                    return manager.endCall();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT > 28) {    //android 10 挂断电话使用反射存在NoSuchMethodError问题
                return false;
            }
        }
        boolean result = false;
        String msg = null;
        try {
            ITelephony iTelephony = getITelephony(context);
            if (iTelephony == null) {
                msg = "failed to hang up,is null";
                return false;
            }
            // 结束电话
            result = iTelephony.endCall();
            if (!result) {
            }
            msg = "telephony end call results=" + result;
        } catch (Exception e) {
            e.printStackTrace();
            String className = null;
            if (e.getClass() != null) {
                className = e.getClass().getName() + e.getMessage();

            }
            msg = "failed to hang up,has exception >>" + className + "(" + result + ")";
            return false;
        }
        return result;
    }


    private static ITelephony getITelephony(Context context) {
        IBinder binder = null;
        Method method = null;
        ITelephony telephony = null;
        try {
            Class<?> classObj = Class.forName("android.os.ServiceManager");
            method = classObj.getMethod("getService", String.class);
            binder = (IBinder) method.invoke(null, new Object[]{"phone"});
            telephony = ITelephony.Stub.asInterface(binder);
        } catch (Exception e1) {
            e1.printStackTrace();
            String className = null;
            if (e1.getClass() != null) {
                className = e1.getClass().getName();
            }
            return null;
        }
        return telephony;
    }
}