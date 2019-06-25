package com.northghost.afvclient.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.anchorfree.hydrasdk.HydraSdk;
import com.anchorfree.hydrasdk.api.caketube.ConnectionType;
import com.anchorfree.hydrasdk.api.data.Country;
import com.anchorfree.hydrasdk.callbacks.Callback;
import com.anchorfree.hydrasdk.exceptions.HydraException;
import com.northghost.afvclient.R;
import com.northghost.afvclient.adapter.RegionListAdapter;
import java.util.List;

public class RegionChooserDialog extends DialogFragment implements RegionListAdapter.RegionListAdapterInterface {

    public static final String TAG = RegionChooserDialog.class.getSimpleName();

    @BindView(R.id.regions_recycler_view)
    RecyclerView regionsRecyclerView;

    @BindView(R.id.regions_progress)
    ProgressBar regionsProgressBar;

    private RegionListAdapter regionAdapter;
    private RegionChooserInterface regionChooserInterface;

    public RegionChooserDialog() {
    }

    public static RegionChooserDialog newInstance() {
        RegionChooserDialog frag = new RegionChooserDialog();
        Bundle args = new Bundle();
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
        return inflater.inflate(R.layout.dialog_region_chooser, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        regionsRecyclerView.setHasFixedSize(true);
        regionsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        regionAdapter = new RegionListAdapter(this);
        regionsRecyclerView.setAdapter(regionAdapter);

        loadServers();
    }

    private void loadServers() {
        showProgress();
        //AFClientService api = ((MainApplication) getActivity().getApplication()).getApi();
        //api.getServers(Protocol.UDP, new ResponseCallback<ServersResponse>() {
        //    @Override
        //    public void success(ServersResponse response) {
        //        if (!"OK".equals(response.getResult())) {
        //            dismiss();
        //        }
        //        regionAdapter.setRegions(response.getCountries());
        //        hideProress();
        //    }
        //
        //    @Override
        //    public void failure(ApiException e) {
        //        dismiss();
        //    }
        //});
        HydraSdk.countries(ConnectionType.OPENVPN_TCP, new Callback<List<Country>>() {
            @Override
            public void success(@NonNull List<Country> countries) {
                regionAdapter.setRegions(countries);
                hideProress();
            }

            @Override
            public void failure(@NonNull HydraException e) {

            }
        });
    }

    private void showProgress() {
        regionsProgressBar.setVisibility(View.VISIBLE);
        regionsRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideProress() {
        regionsProgressBar.setVisibility(View.GONE);
        regionsRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCountrySelected(Country item) {
        regionChooserInterface.onRegionSelected(item);
        dismiss();
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        if (ctx instanceof RegionChooserInterface) {
            regionChooserInterface = (RegionChooserInterface) ctx;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        regionChooserInterface = null;
    }

    public interface RegionChooserInterface {
        void onRegionSelected(Country item);
    }
}
