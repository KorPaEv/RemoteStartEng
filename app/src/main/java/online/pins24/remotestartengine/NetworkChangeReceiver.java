package online.pins24.remotestartengine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

public class NetworkChangeReceiver extends WakefulBroadcastReceiver {

    protected List<NetworkStateReceiverListener> listeners;
    protected Boolean connected;

    public NetworkChangeReceiver() {
        listeners = new ArrayList<NetworkStateReceiverListener>();
        connected = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent == null || intent.getExtras() == null)
            return;

        try {
            boolean isVisible = CustomApplication.isActivityVisible();

            if (isVisible == true) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    connected = true;
                } else {
                    connected = false;
                }
                notifyStateToAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyStateToAll() {
        for(NetworkStateReceiverListener listener : listeners)
            notifyState(listener);
    }

    private void notifyState(NetworkStateReceiverListener listener) {
        if(connected == null || listener == null) return;

        listener.onNetworkStateChanged(connected);
    }

    public void addListener(NetworkStateReceiverListener l) {
        listeners.add(l);
        notifyState(l);
    }

    public void removeListener(NetworkStateReceiverListener l) {
        listeners.remove(l);
    }

    public interface NetworkStateReceiverListener {
        void onNetworkStateChanged(boolean isConnected);
    }
}