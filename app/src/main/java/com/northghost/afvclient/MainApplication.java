package com.northghost.afvclient;

import android.app.Application;
import com.anchorfree.hydrasdk.HydraSDKConfig;
import com.anchorfree.hydrasdk.HydraSdk;
import com.anchorfree.hydrasdk.api.ClientInfo;
import com.anchorfree.hydrasdk.vpnservice.connectivity.NotificationConfig;
import com.northghost.caketube.CaketubeCredentialsSource;
import com.northghost.caketube.CaketubeTransport;
import com.northghost.caketube.CaketubeTransportFactory;

public class MainApplication extends Application {
    private static final String CHANNEL_ID = "vpn_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        //api = AFClientService.newBuilder(this)
        //        .setCarrierId("afdemo")
        //        .setDebugLogging(true)
        //        .setHostUrl("https://backend.northghost.com")
        //        .setVPNNotificationProvider(new VPNNotificationProvider() {
        //            // NOTE: this method will be called from VPN process, reference to MainApplication will be different
        //            @Override
        //            public Notification createVPNNotification(String message, String ticker, boolean b, long l, VpnStatus.ConnectionStatus connectionStatus) {
        //                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        //                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //                        CharSequence name = "VPN";
        //                        String description = "VPN";
        //                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        //                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        //                        channel.setDescription(description);
        //                        // Register the channel with the system; you can't change the importance
        //                        // or other notification behaviors after this
        //                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        //                        notificationManager.createNotificationChannel(channel);
        //                        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
        //                                .setContentText(message)
        //                                .setTicker(ticker)
        //                                .setOnlyAlertOnce(true)
        //                                .setCategory(Notification.CATEGORY_STATUS)
        //                                //.setOngoing(true)
        //                                .setContentTitle("My Super Awesome VPN")
        //                                .setSmallIcon(R.drawable.icon)
        //                                .build();
        //                    }else{
        //                        return new Notification.Builder(getApplicationContext())
        //                                .setContentText(message)
        //                                .setTicker(ticker)
        //                                .setContentTitle("My Super Awesome VPN")
        //                                .setSmallIcon(R.drawable.icon)
        //                                .build();
        //                    }
        //                }
        //                return null;
        //            }
        //        })
        //        .build();
        HydraSdk.init(this, ClientInfo.newBuilder()
                        .carrierId("afdemo")
                        .baseUrl("https://backend.northghost.com")
                        .build(),
                NotificationConfig.newBuilder()
                        .channelId("vpn_channel_id")
                        .build(), HydraSDKConfig.newBuilder()
                        .registerTransport(CaketubeTransport.TRANSPORT_ID_TCP, CaketubeTransportFactory.class, CaketubeCredentialsSource.class)
                        .registerTransport(CaketubeTransport.TRANSPORT_ID_UDP, CaketubeTransportFactory.class, CaketubeCredentialsSource.class)
                        .build());
    }
}
