package com.northghost.afvclient.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.anchorfree.hydrasdk.HydraSdk;
import com.anchorfree.hydrasdk.SessionConfig;
import com.anchorfree.hydrasdk.api.AuthMethod;
import com.anchorfree.hydrasdk.api.data.Country;
import com.anchorfree.hydrasdk.api.data.ServerCredentials;
import com.anchorfree.hydrasdk.api.response.RemainingTraffic;
import com.anchorfree.hydrasdk.api.response.User;
import com.anchorfree.hydrasdk.callbacks.Callback;
import com.anchorfree.hydrasdk.callbacks.CompletableCallback;
import com.anchorfree.hydrasdk.callbacks.VpnStateListener;
import com.anchorfree.hydrasdk.exceptions.HydraException;
import com.anchorfree.hydrasdk.vpnservice.TrafficStats;
import com.anchorfree.hydrasdk.vpnservice.VPNState;
import com.anchorfree.reporting.TrackingConstants;
import com.northghost.afvclient.R;
import com.northghost.afvclient.core.MainHelper;
import com.northghost.afvclient.dialogs.DialogHelper;
import com.northghost.afvclient.dialogs.RegionChooserDialog;
import com.northghost.caketube.CaketubeTransport;

public class MainActivity extends AppCompatActivity
        implements RegionChooserDialog.RegionChooserInterface, VpnStateListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.main_toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.login_btn)
    TextView loginBtnTextView;

    @BindView(R.id.login_state)
    TextView loginStateTextView;

    @BindView(R.id.login_progress)
    ProgressBar loginProgressBar;

    @BindView(R.id.connect_btn)
    TextView connectBtnTextView;

    @BindView(R.id.connection_state)
    TextView connectionStateTextView;

    @BindView(R.id.connection_progress)
    ProgressBar connectionProgressBar;

    @BindView(R.id.traffic_stats)
    TextView trafficStats;

    @BindView(R.id.traffic_limit)
    TextView trafficLimitTextView;

    @BindView(R.id.optimal_server_btn)
    TextView currentServerBtn;

    @BindView(R.id.selected_server)
    TextView selectedServerTextView;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        startTrafficStatsTracker();
        HydraSdk.addVpnListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTrafficStatsTracker();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void disconnectVPN() {
        HydraSdk.stopVPN(TrackingConstants.GprReasons.M_UI, new CompletableCallback() {
            @Override
            public void complete() {

            }

            @Override
            public void error(@NonNull HydraException e) {

            }
        });
    }

    @OnClick(R.id.login_btn)
    public void onLoginBtnClick(View v) {
        showLoginProgress();

        if (!HydraSdk.isLoggedIn()) {
            HydraSdk.login(AuthMethod.anonymous(), new Callback<User>() {
                @Override
                public void success(@NonNull User user) {
                    updateUI();
                }

                @Override
                public void failure(@NonNull HydraException e) {
                    DialogHelper.showMessage(getString(R.string.signin_error), e.getMessage(), MainActivity.this);
                    updateUI();
                }
            });
        } else {
            HydraSdk.logout(new CompletableCallback() {
                @Override
                public void complete() {
                    disconnectVPN();
                    updateUI();
                }

                @Override
                public void error(@NonNull HydraException e) {
                    Toast.makeText(MainActivity.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    updateUI();
                }
            });
        }
    }

    @OnClick(R.id.connect_btn)
    public void onConnectBtnClick(View v) {

        HydraSdk.getVpnState(new Callback<VPNState>() {
            @Override
            public void success(@NonNull VPNState vpnState) {
                if (vpnState == VPNState.CONNECTED) {
                    HydraSdk.stopVPN(TrackingConstants.GprReasons.M_UI, CompletableCallback.EMPTY);
                } else if (vpnState == VPNState.IDLE) {
                    showConnectionProgress();
                    HydraSdk.startVPN(new SessionConfig.Builder()
                            .withTransport(CaketubeTransport.TRANSPORT_ID_UDP)
                            .withReason(TrackingConstants.GprReasons.M_UI)
                            .build(), new Callback<ServerCredentials>() {
                        @Override
                        public void success(@NonNull ServerCredentials serverCredentials) {

                        }

                        @Override
                        public void failure(@NonNull HydraException throwable) {

                            DialogHelper.showMessage(getString(R.string.signin_error), "Unable to connect: " + throwable.getMessage(), MainActivity.this);
                            hideConnectionProgress();
                        }
                    });
                }
            }

            @Override
            public void failure(@NonNull HydraException e) {

            }
        });
    }

    @OnClick(R.id.optimal_server_btn)
    public void onServerChooserClick(View v) {
        RegionChooserDialog.newInstance().show(getSupportFragmentManager(), RegionChooserDialog.TAG);
    }

    private void updateUI() {

        hideLoginProgress();
        hideConnectionProgress();

        boolean hasAuth = HydraSdk.isLoggedIn();
        loginBtnTextView.setText(hasAuth ? R.string.log_out : R.string.log_in);
        loginStateTextView.setText(hasAuth ? R.string.logged_in : R.string.logged_out);

        HydraSdk.getVpnState(new Callback<VPNState>() {
            @Override
            public void success(@NonNull VPNState vpnState) {
                boolean connected = vpnState == VPNState.CONNECTED;
                connectBtnTextView.setText(connected ? R.string.disconnect : R.string.connect);
                connectionStateTextView.setText(connected ? R.string.connected : R.string.disconnected);
            }

            @Override
            public void failure(@NonNull HydraException e) {

            }
        });
    }

    final Runnable trafficStatsUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateTrafficInfo();
        }
    };

    private void updateTrafficInfo() {
        HydraSdk.remainingTraffic(new Callback<RemainingTraffic>() {
            @Override
            public void success(@NonNull final RemainingTraffic remainingTraffic) {
                HydraSdk.getTrafficStats(new Callback<TrafficStats>() {
                    @Override
                    public void success(@NonNull TrafficStats stats) {
                        String inString = MainHelper.humanReadableByteCountOld(stats.getBytesRx(), false);
                        String outString = MainHelper.humanReadableByteCountOld(stats.getBytesTx(), false);
                        trafficStats.setText(getResources().getString(R.string.traffic_stats, inString, outString));

                        String trafficUsed = "UNLIMITED";
                        String trafficLimit = "UNLIMITED";

                        trafficUsed = MainHelper.megabyteCount(remainingTraffic.getTrafficUsed()) + "Mb";

                        if (!remainingTraffic.isUnlimited()) {
                            trafficLimit = MainHelper.megabyteCount(remainingTraffic.getTrafficLimit()) + "Mb";
                        }
                        trafficLimitTextView.setText(getResources().getString(R.string.traffic_limit, trafficUsed, trafficLimit));

                        handler.postDelayed(trafficStatsUpdateRunnable, 1000);
                    }

                    @Override
                    public void failure(@NonNull HydraException e) {

                    }
                });
            }

            @Override
            public void failure(@NonNull HydraException e) {

            }
        });
    }

    private void startTrafficStatsTracker() {
        stopTrafficStatsTracker();

        handler.postDelayed(trafficStatsUpdateRunnable, 1000);
    }

    private void stopTrafficStatsTracker() {
        handler.removeCallbacks(trafficStatsUpdateRunnable);
    }

    private void showLoginProgress() {
        loginProgressBar.setVisibility(View.VISIBLE);
        loginStateTextView.setVisibility(View.GONE);
    }

    private void hideLoginProgress() {
        loginProgressBar.setVisibility(View.GONE);
        loginStateTextView.setVisibility(View.VISIBLE);
    }

    private void showConnectionProgress() {
        connectionProgressBar.setVisibility(View.VISIBLE);
        connectionStateTextView.setVisibility(View.GONE);
    }

    private void hideConnectionProgress() {
        connectionProgressBar.setVisibility(View.GONE);
        connectionStateTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRegionSelected(Country item) {
        currentServerBtn.setText(R.string.current_server);
        selectedServerTextView.setText(item.getCountry());
        selectedServerTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void vpnStateChanged(@NonNull VPNState vpnState) {
        if (vpnState == VPNState.CONNECTED) {
            updateUI();
            startTrafficStatsTracker();
        } else if (vpnState == VPNState.IDLE) {
            updateUI();
            stopTrafficStatsTracker();
        } else {
            showConnectionProgress();
        }
    }

    @Override
    public void vpnError(@NonNull HydraException e) {

    }
}
