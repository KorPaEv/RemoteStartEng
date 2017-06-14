package online.pins24.remotestartengine;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MapFragment extends BaseFragment implements NetworkChangeReceiver.NetworkStateReceiverListener {

    Context appContext;
    Button bGetCoord;
    View rootView;
    TextView tvNetworkState;
    private NetworkChangeReceiver networkChangeReceiver;
    private MapView mapView;
    IMapController mapController;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        appContext = getContext().getApplicationContext();
        findViews();
        setDefaultSettings();
    }

    //region findViews() Поиск вьюх определенных в R.id
    private void findViews()
    {
        rootView = getView();
        bGetCoord = (Button) rootView.findViewById(R.id.bGetCoordinates);
        tvNetworkState = (TextView) rootView.findViewById(R.id.tvNetworkState);
    }
    //endregion

    private void setDefaultSettings()
    {
        networkReceiverInit();
        onFirstLoadNetworkStatus();
        loadMap();
    }

    private void networkReceiverInit() {
        networkChangeReceiver = new NetworkChangeReceiver();
        networkChangeReceiver.addListener(this);
        appContext.registerReceiver(networkChangeReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void onFirstLoadNetworkStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            changeTextStatus(true);
        } else {
            changeTextStatus(false);
        }
    }

    private void loadMap() {
        appContext = getActivity();
        mapView = new MapView(appContext, null);
        mapView.getTileProvider().setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(10);
        GeoPoint startPoint = new GeoPoint(54, 55);
        mapController.setCenter(startPoint);
        RelativeLayout relativeLayout = (RelativeLayout) getActivity().findViewById(R.id.map_layout);
        mapView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        relativeLayout.addView(mapView);
    }

    // Меняем текст вьюхи - в сети или нет
    public void changeTextStatus(boolean isConnected) {
        if (isConnected) {
            tvNetworkState.setText(getString(R.string.networkStateOn));
            tvNetworkState.setTextColor(getResources().getColor(R.color.colorNetworkOn));
        } else {
            tvNetworkState.setText(getString(R.string.networkStateOff));
            tvNetworkState.setTextColor(getResources().getColor(R.color.colorNetworkOff));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CustomApplication.activityPaused();// On Pause notify the Application
    }

    @Override
    public void onResume() {
        super.onResume();
        CustomApplication.activityResumed();// On Resume notify the Application
    }

    @Override
    public void onNetworkStateChanged(boolean isConnected) {
        changeTextStatus(isConnected);
    }
}
