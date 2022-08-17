package psycho.euphoria.blocker;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MainActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermissions(new String[]{
                "android.permission.READ_PHONE_STATE",
                "android.permission.CALL_PHONE",
                "android.permission.READ_CALL_LOG",
                "android.permission.ANSWER_PHONE_CALLS"
        }, 1);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pre);
    }
}