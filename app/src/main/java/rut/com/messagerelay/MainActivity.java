package rut.com.messagerelay;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import rut.com.messagerelay.UserData.Azure;
import rut.com.messagerelay.UserData.Location;
import rut.com.messagerelay.WiFiDirect.WiFiDirectService;

public class MainActivity extends AppCompatActivity {

    public static final int LOCATION_PERMISSION_REQUEST = 0;
    Location location;

    WiFiDirectService wiFiDirectService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Azure azure = new Azure(this);
        azure.setup();
        if (!azure.loadUserTokenCache()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_main);
            location = new Location(this);
            location.setup(this);

            if (!azure.loadData()) {
                Toast.makeText(this, "Error loading dataset", Toast.LENGTH_SHORT).show();
            } else {
                //BackgroundServices.scheduleJobCheckEmergency(this);
                wiFiDirectService = new WiFiDirectService(this);
                wiFiDirectService.setup();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new Location(this).setup(this);
        }
    }

    @Override
    public void onDestroy() {
        if (wiFiDirectService != null) wiFiDirectService.unregisterReceiver();
        super.onDestroy();
    }

}