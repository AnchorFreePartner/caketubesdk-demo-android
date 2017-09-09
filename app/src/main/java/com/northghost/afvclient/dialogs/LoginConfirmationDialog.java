package com.northghost.afvclient.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.northghost.afvclient.BuildConfig;
import com.northghost.afvclient.MainApplication;
import com.northghost.afvclient.R;

public class LoginConfirmationDialog extends DialogFragment {

    public static final String TAG = LoginConfirmationDialog.class.getSimpleName();

    private static final String KEY_OAUTH_TOKEN = "key:oauth_token";
    private static final String KEY_OAUTH_METHOD = "key:oauth_method";

    @BindView(R.id.host_url_ed)
    EditText hostUrlEditText;

    @BindView(R.id.carrier_id_ed)
    EditText carrierIdEditText;

    @BindView(R.id.oauth_access_token_ed)
    EditText accessTokenEditText;

    @BindView(R.id.oauth_access_method_ed)
    EditText accessMethodEditText;

    LoginConfirmationInterface loginConfirmationInterface;

    public LoginConfirmationDialog() {
    }

    public static LoginConfirmationDialog newInstance(String token, String method) {
        LoginConfirmationDialog frag = new LoginConfirmationDialog();
        Bundle args = new Bundle();
        if (token != null) args.putString(KEY_OAUTH_TOKEN, token);
        if (method != null) args.putString(KEY_OAUTH_METHOD, method);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_login_confirmation, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        SharedPreferences prefs = ((MainApplication) getActivity().getApplication()).getPrefs();

        hostUrlEditText.setText(prefs.getString(BuildConfig.STORED_HOST_URL_KEY, BuildConfig.BASE_HOST));
        carrierIdEditText.setText(prefs.getString(BuildConfig.STORED_CARRIER_ID_KEY, BuildConfig.BASE_CARRIER_ID));

        accessTokenEditText.setText(getArguments().getString(KEY_OAUTH_TOKEN, ""));
        accessMethodEditText.setText(getArguments().getString(KEY_OAUTH_METHOD, BuildConfig.BASE_OAUTH_METHOD));

        // Show soft keyboard automatically and request focus to field
        hostUrlEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        if (ctx instanceof LoginConfirmationInterface) {
            loginConfirmationInterface = (LoginConfirmationInterface) ctx;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loginConfirmationInterface = null;
    }

    @OnClick(R.id.login_btn)
    public void onLoginBtnClick(View v) {
        String hostUrl = hostUrlEditText.getText().toString();
        if (hostUrl.equals("")) hostUrl = BuildConfig.BASE_HOST;
        String carrierId = carrierIdEditText.getText().toString();
        if (carrierId.equals("")) carrierId = BuildConfig.BASE_CARRIER_ID;

        String token = accessTokenEditText.getText().toString();
        if (token.equals("")) token = null;
        String method = accessMethodEditText.getText().toString();
        if (method.equals("")) method = BuildConfig.BASE_OAUTH_METHOD;

        loginConfirmationInterface.setLoginParams(hostUrl, carrierId, token, method);
        loginConfirmationInterface.loginUser();
        dismiss();
    }

    public interface LoginConfirmationInterface {
        void setLoginParams(String hostUrl, String carrierId, String token, String method);

        void loginUser();
    }
}
