package rut.com.messagerelay;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import rut.com.messagerelay.UserData.Azure;
import rut.com.messagerelay.UserData.Location;
import rut.com.messagerelay.WiFiDirect.WiFiDirectService;

public class MainActivity extends AppCompatActivity {

    public static final int LOCATION_SERVICE_REQUEST = 0;

    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup WiFiDirect
        WiFiDirectService wiFiDirectService = new WiFiDirectService(this);
        wiFiDirectService.setup();

        //Setup fetching location
        location = new Location(this);
        location.setup();

        Azure azure = new Azure(this);
        azure.connect();
        azure.test();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == LOCATION_SERVICE_REQUEST) {
            location.setup();
        }
    }

}