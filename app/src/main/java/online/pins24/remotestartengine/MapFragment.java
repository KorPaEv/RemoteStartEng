package online.pins24.remotestartengine;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MapFragment extends BaseFragment implements NetworkChangeReceiver.NetworkStateReceiverListener {

    private String strCurrDateTime; //текущая дата время
    private int mapZoom; //зум для карты
    private float latCoord, lngCoord; //широта долгота
    private final float LATDEFAULT = 0; //значение для широты по умолчанию
    private final float LNGDEFAULT = 0;
    private final int NULLTMAPZOOM = 2; //значение зума для карты когда у нас не было еще запроса координат
    private final int DEFAULTMAPZOOM = 16; //значение зума для карты когда у нас пришли координаты по запросу
    private final String SHAREDPREF = "SharedPref";
    private final String DATETIMEREQUESTCOORDSHAREDPREF = "dateRequestCoordSharedPref";
    private final String LATSHAREDPREF = "latSharedPref";
    private final String LNGSHAREDPREF = "lngSharedPref";

    SharedPreferences sharedPref;
    Context appContext;
    Context appActivity;
    Button bGetCoord;
    View rootView;
    TextView tvNetworkState;
    private RelativeLayout mapLayout; //на этом слое лежит компонент карты
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;
    private CustomToast customToast; //кастомный тост, смотри класс CustomToast
    private NetworkChangeReceiver networkChangeReceiver; //рессивер отслеживания состояния сети
    private ConnectivityManager connectivityManager; //манагер для сети
    private NetworkInfo networkInfo;
    private MapView mapView; //карта
    private IMapController mapController; //контроллер карты
    private GeoPoint startPoint; //точка координат
    private ItemizedOverlayWithFocus<OverlayItem> itemItemizedOverlayWithFocus; //старая версия вывода маркера на карту, смотри ремареный код в loadMap()

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        findViews();
        fillData();
    }

    private void init() {
        appContext = getContext().getApplicationContext();
        appActivity = getActivity();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        customToast = new CustomToast(appContext);
        networkReceiverInit();
    }

    //region networkReceiverInit() Инициализируем рессивер сети
    private void networkReceiverInit() {
        networkChangeReceiver = new NetworkChangeReceiver();
        networkChangeReceiver.addListener(this);
        appContext.registerReceiver(networkChangeReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }
    //endregion

    //region findViews() Поиск вьюх определенных в R.id
    private void findViews() {
        rootView = getView();
        bGetCoord = (Button) rootView.findViewById(R.id.bGetCoordinates);
        tvNetworkState = (TextView) rootView.findViewById(R.id.tvNetworkState);
        bGetCoord.setOnClickListener(buttonClickListener);
    }
    //endregion

    //region OnClickListener листенер на клик кнопок
    public View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bGetCoordinates:
                    requestCoordinates();
                    break;
            }
        }
    };
    //endregion

    private void requestCoordinates() {
        //Допустим что по запросу нам пришли координаты
        strCurrDateTime = simpleDateFormat.format(calendar.getTime()); //получили текущее время
        latCoord = (float) 53.35; //получили координаты
        lngCoord = (float) 83.76;
        saveSharedPref(); //сохранили данные последнего запроса
        fillData(); //загрузились
    }

    private void fillData() {
        Toast.makeText(appContext, "Загрузка...", Toast.LENGTH_SHORT).show();
        firstLoadNetworkStatus(); //отследили сеть
        loadSharedPref(); //прочитали настройки

        //исходя из того что прочитали определились с зумом на карте
        mapZoom = NULLTMAPZOOM;
        if (latCoord != 0 && lngCoord != 0) {
            mapZoom = DEFAULTMAPZOOM;
        }

        loadMap(); //загрузили карту
    }

    //region firstLoadNetworkStatus() Отрисовали на экране состояние сети
    private void firstLoadNetworkStatus() {
        if (isNetworkOnline()) {
            changeTextStatus(true);
        } else {
        changeTextStatus(false);
        }
    }
    //endregion

    //region firstLoadNetworkStatus() Проверяем экране состояние сети
    private boolean isNetworkOnline() {
        connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
    //endregion

    //region loadMap() Грузим карту
    private void loadMap() {
        mapView = new MapView(appActivity.getApplicationContext());

        mapView.getTileProvider().setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(true);
        mapController = mapView.getController();
        mapController.setZoom(mapZoom);
        startPoint = new GeoPoint(latCoord, lngCoord);

        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.mipmap.map_car_icon));
        startMarker.setTitle(String.format("%s\n%s", getString(R.string.locationCarRequest), strCurrDateTime));
        mapView.getOverlays().add(startMarker);
        mapView.invalidate();
//        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
//        items.add(new OverlayItem("Here", "SampleDescription", startPoint));
//
//        itemItemizedOverlayWithFocus = new ItemizedOverlayWithFocus<OverlayItem>(
//                appActivity,
//                items,
//                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
//                    @Override
//                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
//                        //do something
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onItemLongPress(final int index, final OverlayItem item) {
//                        return false;
//                    }
//                });
//        itemItemizedOverlayWithFocus.setFocusItemsOnTap(true);
//        mapView.getOverlays().add(itemItemizedOverlayWithFocus);
        mapController.setCenter(startPoint);
        mapLayout = (RelativeLayout) getActivity().findViewById(R.id.map_layout);
        mapLayout.addView(mapView);
    }
    //endregion

    //region changeTextStatus Меняем текст вьюхи - в сети или нет
    public void changeTextStatus(boolean isConnected) {
        if (isConnected) {
            tvNetworkState.setText(getString(R.string.networkStateOn));
            tvNetworkState.setTextColor(getResources().getColor(R.color.colorNetworkOn));
        } else {
            tvNetworkState.setText(getString(R.string.networkStateOff));
            tvNetworkState.setTextColor(getResources().getColor(R.color.colorNetworkOff));
        }
    }
    //endregion

    //region saveSharedPref() Сохраняем настройки
    private void saveSharedPref() {
        Toast.makeText(appContext, "Сохраняем...", Toast.LENGTH_SHORT).show();
        //Создаем объект Editor для создания пар имя-значение:
        sharedPref = appContext.getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);
        //Создаем объект Editor для создания пар имя-значение:
        SharedPreferences.Editor shpEditor = sharedPref.edit();
        shpEditor.putString(DATETIMEREQUESTCOORDSHAREDPREF, strCurrDateTime);
        shpEditor.putFloat(LATSHAREDPREF, latCoord);
        shpEditor.putFloat(LNGSHAREDPREF, lngCoord);
        shpEditor.commit();
    }
    //endregion

    //region loadSharedPref() Читаем сохраненные настройки
    private void loadSharedPref() {
        //Используем созданный файл данных SharedPreferences:
        sharedPref = appContext.getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);
        strCurrDateTime = sharedPref.getString(DATETIMEREQUESTCOORDSHAREDPREF, getString(R.string.emptyVal));
        latCoord = sharedPref.getFloat(LATSHAREDPREF, LATDEFAULT);
        lngCoord = sharedPref.getFloat(LNGSHAREDPREF, LNGDEFAULT);
    }
    //endregion

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
