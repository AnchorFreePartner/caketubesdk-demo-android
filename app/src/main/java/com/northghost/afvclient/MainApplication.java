package com.northghost.afvclient;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.northghost.caketube.AFClientService;

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
