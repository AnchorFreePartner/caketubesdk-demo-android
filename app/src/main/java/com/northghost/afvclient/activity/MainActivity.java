package com.northghost.afvclient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.northghost.afvclient.MainApplication;
import com.northghost.afvclient.R;
import com.northghost.afvclient.core.CodeStrings;
import com.northghost.afvclient.core.MainHelper;
import com.northghost.afvclient.dialogs.DialogHelper;
import com.northghost.afvclient.dialogs.RegionChooserDialog;
import com.northghost.caketube.AFClientService;
import com.northghost.caketube.AFConnectionService;
import com.northghost.caketube.ApiException;
import com.northghost.caketube.Protocol;
import com.northghost.caketube.ResponseCallback;
import com.northghost.caketube.pojo.CredentialsResponse;
import com.northghost.caketube.pojo.LoginResponse;
import com.northghost.caketube.pojo.LogoutResponse;
import com.northghost.caketube.pojo.RemainingTrafficResponse;
import com.northghost.caketube.pojo.ServerItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements AFConnectionService.ServiceConnectionCallbacks,
        AFConnectionService.VPNConnectionStateListener,
        RegionChooserDialog.RegionChooserInterface {

    private static final String TAG = MainActivity.class.getSimpleName();

    private AFConnectionService connectionService;

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

        connectionService = AFConnectionService.newBuilder(this)
                .addConnectionCallbacksListener(this)
                .addVPNConnectionStateListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectionService.onStart(); // NOTE: this is a mandatory call
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        startTrafficStatsTracker();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTrafficStatsTracker();
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectionService.onStop(); // NOTE: this is a mandatory call
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        connectionService.onActivityResult(requestCode, resultCode, data); // NOTE: this is a mandatory call
    }

    private void disconnectVPN() {
        connectionService.disconnect();
    }

    @Override
    public void onConnected() {
        updateUI();
        updateTrafficInfo();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onVPNConnectionStateChanged(AFConnectionService.VPNConnectionState vpnConnectionState) {
        if (vpnConnectionState == AFConnectionService.VPNConnectionState.CONNECTING) {
            showConnectionProgress();
        } else if (vpnConnectionState == AFConnectionService.VPNConnectionState.CONNECTED) {
            updateUI();
            startTrafficStatsTracker();
        } else if (vpnConnectionState == AFConnectionService.VPNConnectionState.NOT_CONNECTED) {
            updateUI();
            stopTrafficStatsTracker();
        }
    }

    @Override
    public void onVPNPermissionRequested() {
    }

    @Override
    public void onVPNPermissionGranted(boolean granted) {
        if (!granted) {
            DialogHelper.showMessage(getString(R.string.signin_error), "You haven't granted VPN permission, please allow this app to connect VPN", this);
            disconnectVPN();
        }
    }

    @OnClick(R.id.login_btn)
    public void onLoginBtnClick(View v) {
        AFClientService api = ((MainApplication) getApplication()).getApi();
        showLoginProgress();

        if (!api.isLoggedIn()) {
            api.login(null, "anonymous", new ResponseCallback<LoginResponse>() {
                @Override
                public void success(LoginResponse loginResponse) {
                    if (!"OK".equalsIgnoreCase(loginResponse.getResult())) {
                        int stringRes = CodeStrings.codeForStatus(loginResponse.getResult());
                        String error = (stringRes == R.string.app_error) ? loginResponse.getResult() : getString(stringRes);

                        DialogHelper.showMessage(getString(R.string.signin_error), error, MainActivity.this);
                        return;
                    }
                    updateUI();
                }

                @Override
                public void failure(ApiException e) {
                    Log.e("loginUser", "unable to sign in " + e);
                    DialogHelper.showMessage(getString(R.string.signin_error), e.getMessage(), MainActivity.this);
                    updateUI();
                }
            });
        } else {
            api.logout(new ResponseCallback<LogoutResponse>() {
                @Override
                public void success(LogoutResponse logoutResponse) {
                    if (connectionService.isServiceConnected() && connectionService.getVPNConnectionState() != AFConnectionService.VPNConnectionState.NOT_CONNECTED) {
                        connectionService.disconnect();
                    }
                    updateUI();
                }

                @Override
                public void failure(ApiException e) {
                    Toast.makeText(MainActivity.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    updateUI();
                }
            });
        }
    }

    @OnClick(R.id.connect_btn)
    public void onConnectBtnClick(View v) {
        if (connectionService.getVPNConnectionState() == AFConnectionService.VPNConnectionState.NOT_CONNECTED) {
            showConnectionProgress();
            final AFClientService api = ((MainApplication) getApplication()).getApi();
            api.getCredentials(Protocol.AUTO, new ResponseCallback<CredentialsResponse>() {
                @Override
                public void success(CredentialsResponse credentialsResponse) {
                    connectionService.connect(credentialsResponse, MainActivity.this);
                }

                @Override
                public void failure(ApiException exception) {
                    Throwable throwable = exception;
                    if (exception.getCause() != null) {
                        throwable = exception.getCause();
                    }

                    DialogHelper.showMessage(getString(R.string.signin_error), "Unable to connect: " + throwable.getMessage(), MainActivity.this);

                    hideConnectionProgress();

                    if (throwable instanceof com.northghost.caketube.exceptions.UnauthorizedException) {
                        ((MainApplication) getApplication()).getApi().destroySession();
                        updateUI();
                    }
                }
            });
        } else if (connectionService.getVPNConnectionState() == AFConnectionService.VPNConnectionState.CONNECTED) {
            showConnectionProgress();
            connectionService.disconnect();
        } else {
            // We ignore transitive connection state VPNConnectionState.CONNECTING
        }
    }

    @OnClick(R.id.optimal_server_btn)
    public void onServerChooserClick(View v) {
        RegionChooserDialog.newInstance().show(getSupportFragmentManager(), RegionChooserDialog.TAG);
    }

    private void updateUI() {
        AFClientService api = ((MainApplication) getApplication()).getApi();

        hideLoginProgress();
        hideConnectionProgress();

        boolean hasAuth = api != null && api.isLoggedIn();
        loginBtnTextView.setText(hasAuth ? R.string.log_out : R.string.log_in);
        loginStateTextView.setText(hasAuth ? R.string.logged_in : R.string.logged_out);

        boolean connected = (connectionService != null &&
                connectionService.getVPNConnectionState() == AFConnectionService.VPNConnectionState.CONNECTED);
        connectBtnTextView.setText(connected ? R.string.disconnect : R.string.connect);
        connectionStateTextView.setText(connected ? R.string.connected : R.string.disconnected);
    }

    final Runnable trafficStatsUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateTrafficInfo();
        }
    };

    private void updateTrafficInfo() {
        AFClientService api = ((MainApplication) getApplication()).getApi();
        if (api.isLoggedIn()) {
            ServerItem currentCountry = ((MainApplication) getApplication()).getApi().getCountry();
            if (currentCountry != null) {
                selectedServerTextView.setText(currentCountry.getCountry());
            }
            api.getRemainingTraffic(new ResponseCallback<RemainingTrafficResponse>() {
                @Override
                public void success(RemainingTrafficResponse remainingTrafficResponse) {
                    AFConnectionService.TrafficStats stats = connectionService.getTrafficStats();

                    long inMb = stats.getBytesIn();
                    long outMb = stats.getBytesOut();

                    String inString = MainHelper.humanReadableByteCountOld(inMb, false);
                    String outString = MainHelper.humanReadableByteCountOld(outMb, false);
                    trafficStats.setText(getResources().getString(R.string.traffic_stats, inString, outString));

                    String trafficUsed = "UNLIMITED";
                    String trafficLimit = "UNLIMITED";
                    if (remainingTrafficResponse.getTrafficUsed() != null) {
                        trafficUsed = MainHelper.megabyteCount(remainingTrafficResponse.getTrafficUsed()) + "Mb";
                    }
                    if (!"UNLIMITED".equals(remainingTrafficResponse.getResult())) {
                        trafficLimit = MainHelper.megabyteCount(remainingTrafficResponse.getTrafficLimit()) + "Mb";
                    }
                    trafficLimitTextView.setText(getResources().getString(R.string.traffic_limit, trafficUsed, trafficLimit));

                    handler.postDelayed(trafficStatsUpdateRunnable, 1000);
                }

                @Override
                public void failure(ApiException e) {

                }
            });
        }
    }

    private void startTrafficStatsTracker() {
        stopTrafficStatsTracker();
        if (connectionService != null && connectionService.isServiceConnected()) {
            handler.postDelayed(trafficStatsUpdateRunnable, 1000);
        }
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
    public void onRegionSelected(ServerItem item) {
        ((MainApplication) getApplication()).getApi().setCountry(item);
        currentServerBtn.setText(R.string.current_server);
        selectedServerTextView.setText(item.getCountry());
        selectedServerTextView.setVisibility(View.VISIBLE);
    }
}
