package com.northghost.afvclient;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.northghost.caketube.AFClientService;
import com.northghost.caketube.VPNNotificationProvider;
import de.blinkt.openvpn.core.VpnStatus;

public class MainApplication extends Application {
    private AFClientService api;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public AFClientService getApi() {
        if (api == null) {
            createApi();
        }
        return api;
    }

    public void createApi() {
        SharedPreferences prefs = getPrefs();
        createApi(
                prefs.getString(BuildConfig.STORED_HOST_URL_KEY, BuildConfig.BASE_HOST),
                prefs.getString(BuildConfig.STORED_CARRIER_ID_KEY, BuildConfig.BASE_CARRIER_ID)
        );
    }

    public void createApi(String hostUrl, String carrierId) {
        destroySession();

        setHostAndCarrierId(hostUrl, carrierId);

        api = AFClientService.newBuilder(this)
                .setCarrierId(carrierId)
                .setConnectionRetries(3)
                .setHostUrl(hostUrl)
                .setVPNNotificationProvider(new VPNNotificationProvider() {
                    // NOTE: this method will be called from VPN process, reference to MainApplication will be different
                    @Override
                    public Notification createVPNNotification(String message, String ticker, boolean b, long l, VpnStatus.ConnectionStatus connectionStatus) {
                        return new NotificationCompat.Builder(getApplicationContext(), "ctsdk")
                                .setContentText(message)
                                .setTicker(ticker)
                                .setContentTitle(getResources().getString(R.string.app_name))
                                .setSmallIcon(R.drawable.notification_icon)
                                .build();
                    }
                })
                .build();
    }

    public void destroySession() {
        if (api != null) {
            api.destroySession();
        }
    }

    public SharedPreferences getPrefs() {
        return getSharedPreferences(BuildConfig.SHARED_PREFS, Context.MODE_PRIVATE);
    }

    private void setHostAndCarrierId(String hostUrl, String carrierId) {
        SharedPreferences prefs = getPrefs();
        if (TextUtils.isEmpty(hostUrl)) {
            prefs.edit().remove(BuildConfig.STORED_HOST_URL_KEY).apply();
        } else {
            prefs.edit().putString(BuildConfig.STORED_HOST_URL_KEY, hostUrl).apply();
        }

        if (TextUtils.isEmpty(carrierId)) {
            prefs.edit().remove(BuildConfig.STORED_CARRIER_ID_KEY).apply();
        } else {
            prefs.edit().putString(BuildConfig.STORED_CARRIER_ID_KEY, carrierId).apply();
        }
    }
}
