package us.shiroyama.android.shoebill;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import us.shiroyama.android.shoebill.annotations.WrapStatic;

/**
 * Typical awful "Helper" class that only has static methods which are extremely painful to mock
 *
 * @author Fumihiko Shiroyama
 */

@WrapStatic
public class Helper {
    public static boolean isConnectedWifi(@NonNull Context context) {
        return getActiveNetworkInfo(context).getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isConnected3G(@NonNull Context context) {
        return getActiveNetworkInfo(context).getType() == ConnectivityManager.TYPE_MOBILE;
    }

    @NonNull
    private static NetworkInfo getActiveNetworkInfo(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }
}
