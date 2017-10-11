package com.northghost.afvclient;

import android.app.Application;
import android.app.Notification;
import android.support.v4.app.NotificationCompat;

import com.northghost.caketube.AFClientService;
import com.northghost.caketube.VPNNotificationProvider;

import de.blinkt.openvpn.core.VpnStatus;

public class MainApplication extends Application {
    private AFClientService api;

    @Override
    public void onCreate() {
        super.onCreate();

        api = AFClientService.newBuilder(this)
                .setCarrierId("afdemo")
                .setHostUrl("https://backend.northghost.com")
                .setVPNNotificationProvider(new VPNNotificationProvider() {
                    // NOTE: this method will be called from VPN process, reference to MainApplication will be different
                    @Override
                    public Notification createVPNNotification(String message, String ticker, boolean b, long l, VpnStatus.ConnectionStatus connectionStatus) {
                        return new NotificationCompat.Builder(getApplicationContext(), "af")
                                .setContentText(message)
                                .setTicker(ticker)
                                .setContentTitle("Your awesome VPN title")
                                .setSmallIcon(R.drawable.ic_stat_ac_unit)
                                .build();
                    }
                })
                .build();
    }

    public AFClientService getApi() {
        return api;
    }
}
