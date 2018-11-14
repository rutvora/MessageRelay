package rut.com.messagerelay.WiFiDirect;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import rut.com.messagerelay.UserData.Azure;
import rut.com.messagerelay.UserData.Data;
import rut.com.messagerelay.UserData.DataManipulator;
import rut.com.messagerelay.UserData.StaticData;

public class WiFiDirectService implements WifiP2pManager.ActionListener, WifiP2pManager.DnsSdServiceResponseListener, WifiP2pManager.DnsSdTxtRecordListener {

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private WifiP2pManager wifiP2pManager;
    private Context context;

    public WiFiDirectService(Context context) {
        this.context = context;
    }

    public void setup() {
        setupIntentFilters();
        setupBroadcastReceiver();
        WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            wifiManager.setWifiEnabled(true);
        } else if (wifiManager == null)
            Toast.makeText(context, "Error accessing WiFi hardware", Toast.LENGTH_SHORT).show();
        registerLocalService();
        discoverService();
    }

    public void setIsWifiP2pEnabled(boolean b) {
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
        wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(context, context.getMainLooper(), null);
        WiFiDirectBroadcastReceiver receiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        context.registerReceiver(receiver, intentFilter);
    }

    private void registerLocalService() {
        HashMap<String, String> record = new HashMap<>();
        record.put("messageRelay", new String(new DataManipulator().getByteArray()));  //TODO: Test this
        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "_presense._tcp", record);   //TODO: Understand this
        wifiP2pManager.addLocalService(channel, serviceInfo, this);
    }

    @Override
    public void onSuccess() {
        //Leave blank
    }

    @Override
    public void onFailure(int reason) {
        Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show();
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
        //Log.d(srcDevice.deviceName, txtRecordMap.get("testdata"));
        Azure azure = new Azure(context);
        azure.setup();
        azure.setupSync();
        HashMap<String, Data> receivedMap = new DataManipulator().getHashMap(txtRecordMap.get("messageRelay").getBytes());      //TODO: Test this
        for (String key : receivedMap.keySet()) {
            Data data = receivedMap.get(key);
            if (StaticData.userData.containsKey(key)) {
                if (StaticData.userData.get(key).updatedAt.compareTo(data.updatedAt) < 0) {
                    StaticData.userData.remove(key);
                    StaticData.userData.put(key, data);

                    azure.updateData(data);
                }
            } else {
                StaticData.userData.put(key, data);
                azure.insertData(data);
            }
        }
    }
}
