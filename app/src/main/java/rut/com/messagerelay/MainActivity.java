package rut.com.messagerelay;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;

import rut.com.messagerelay.UserData.Data;
import rut.com.messagerelay.UserData.Location;

public class MainActivity extends AppCompatActivity {
    public static String id = null;

    public static HashMap<String, Data> userData;

    public static final int LOCATION_SERVICE_REQUEST = 0;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        location = new Location(this);
        location.setup(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == LOCATION_SERVICE_REQUEST) {
            location.setup(this);
        }
    }

}