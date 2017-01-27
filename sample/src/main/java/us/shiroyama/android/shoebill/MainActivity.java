package us.shiroyama.android.shoebill;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Sample Activity
 *
 * @author Fumihiko Shiroyama
 */

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean wifiStatus = HelperWrapper.getInstance().isConnectedWifi(getApplicationContext());
        boolean mobileStatus = HelperWrapper.getInstance().isConnected3G(getApplicationContext());

        ((TextView) findViewById(R.id.wifi_status)).setText(String.valueOf(wifiStatus));
        ((TextView) findViewById(R.id.mobile_status)).setText(String.valueOf(mobileStatus));
    }
}
