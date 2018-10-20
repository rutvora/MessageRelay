package rut.com.messagerelay;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements WifiP2pManager.ActionListener,
        WifiP2pManager.PeerListListener, WifiP2pManager.DnsSdServiceResponseListener, WifiP2pManager.DnsSdTxtRecordListener {

    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel channel;
    WifiP2pManager wifiP2pManager;
    WifiDirectBroadcastReceiver receiver;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    HashMap<String, String> buddies = new HashMap<>();
    private WifiP2pDnsSdServiceRequest serviceRequest;

    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

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

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    private void setupBroadcastReceiver() {
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupIntentFilters();
        setupBroadcastReceiver();
        startPeerDiscovery();
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WifiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void startPeerDiscovery() {
        wifiP2pManager.discoverPeers(channel, this);
    }

    @Override
    public void onSuccess() {
        //Leave blank
    }

    @Override
    public void onFailure(int reason) {
        Toast.makeText(this, "Error searching for peers", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        List<WifiP2pDevice> refreshedPeers = new ArrayList<>(peerList.getDeviceList());
        if (!refreshedPeers.equals(peers)) {
            peers.clear();
            peers.addAll(refreshedPeers);
        }

        if (peers.size() == 0) {
            //TODO
        }
        discoverService();
    }

    private void discoverService() {
        wifiP2pManager.setDnsSdResponseListeners(channel, this, this);
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        wifiP2pManager.addServiceRequest(channel, serviceRequest, this);
        wifiP2pManager.discoverServices(channel, this);


    }

    @Override
    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
        //TODO
    }

    @Override
    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
        buddies.put(srcDevice.deviceAddress, txtRecordMap.get("buddyname"));
        //TODO
    }

}