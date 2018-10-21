package rut.com.messagerelay.WiFiDirect;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class WiFiDirectService implements WifiP2pManager.ActionListener, WifiP2pManager.DnsSdServiceResponseListener, WifiP2pManager.DnsSdTxtRecordListener {

    private final IntentFilter intentFilter = new IntentFilter();
    private WiFiDirectBroadcastReceiver receiver;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager wifiP2pManager;
    private boolean isWifiP2pEnabled = false;
    private AppCompatActivity activity;

    public WiFiDirectService(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void setup() {
        if (isWifiP2pEnabled) {
            setupIntentFilters();
            setupBroadcastReceiver();
            registerLocalService();
            discoverService();
        } else {
            Toast.makeText(activity, "WiFi Direct not enabled/available", Toast.LENGTH_SHORT).show();
        }
    }

    public void setIsWifiP2pEnabled(boolean b) {
        isWifiP2pEnabled = b;
    }

    private void setupIntentFilters() {
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);


        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void setupBroadcastReceiver() {
        wifiP2pManager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(activity, activity.getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        activity.registerReceiver(receiver, intentFilter);
    }

    private void registerLocalService() {
        HashMap<String, String> record = new HashMap<>();
        record.put("testdata", "somedata");  //TODO: Fill relevant data
        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "_presense._tcp", record);   //TODO: Understand this
        wifiP2pManager.addLocalService(channel, serviceInfo, this);
    }

    @Override
    public void onSuccess() {
        //Leave blank
    }

    @Override
    public void onFailure(int reason) {
        Toast.makeText(activity, "Error!", Toast.LENGTH_SHORT).show();
        Log.d("Failure", reason + "");
    }

    private void discoverService() {
        wifiP2pManager.setDnsSdResponseListeners(channel, this, this);
        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        wifiP2pManager.removeServiceRequest(channel, serviceRequest, this);

        wifiP2pManager.addServiceRequest(channel, serviceRequest, this);
        wifiP2pManager.discoverServices(channel, this);


    }

    @Override
    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
        Log.d(srcDevice.deviceAddress, srcDevice.deviceName);
        //TODO: Check whether this is useful
    }

    @Override
    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
        //buddies.put(srcDevice.deviceAddress, txtRecordMap.get("testdata"));
        Log.d(srcDevice.deviceName, txtRecordMap.get("testdata"));
        //TODO: handle data receiving
    }
}