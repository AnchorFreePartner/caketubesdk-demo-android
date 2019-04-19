package com.northghost.afvclient;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.northghost.caketube.AFClientService;
import com.northghost.caketube.VPNNotificationProvider;

import de.blinkt.openvpn.core.VpnStatus;

    public class MainApplication extends Application {
    private AFClientService api;
    private static final String CHANNEL_ID = "vpn_channel";
    @Override
    public void onCreate() {
        super.onCreate();

        api = AFClientService.newBuilder(this)
                .setCarrierId("afdemo")
                .setDebugLogging(true)
                .setHostUrl("https://backend.northghost.com")
                .setVPNNotificationProvider(new VPNNotificationProvider() {
                    // NOTE: this method will be called from VPN process, reference to MainApplication will be different
                    @Override
                    public Notification createVPNNotification(String message, String ticker, boolean b, long l, VpnStatus.ConnectionStatus connectionStatus) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                CharSequence name = "VPN";
                                String description = "VPN";
                                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                                channel.setDescription(description);
                                // Register the channel with the system; you can't change the importance
                                // or other notification behaviors after this
                                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                                notificationManager.createNotificationChannel(channel);
                                return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                                        .setContentText(message)
                                        .setTicker(ticker)
                                        .setOnlyAlertOnce(true)
                                        .setCategory(Notification.CATEGORY_STATUS)
                                        //.setOngoing(true)
                                        .setContentTitle("My Super Awesome VPN")
                                        .setSmallIcon(R.drawable.icon)
                                        .build();
                            }else{
                                return new Notification.Builder(getApplicationContext())
                                        .setContentText(message)
                                        .setTicker(ticker)
                                        .setContentTitle("My Super Awesome VPN")
                                        .setSmallIcon(R.drawable.icon)
                                        .build();
                            }
                        }
                        return null;
                    }
                })
                .build();
    }

    public AFClientService getApi() {
        return api;
    }
}
