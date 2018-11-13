package rut.com.messagerelay;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import rut.com.messagerelay.UserData.Azure;
import rut.com.messagerelay.UserData.Data;
import rut.com.messagerelay.UserData.Location;
import rut.com.messagerelay.UserData.StaticData;

public class MainActivity extends AppCompatActivity {

    public static final int LOCATION_SERVICE_REQUEST = 0;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Azure azure = new Azure(this);
        azure.setup();
        if (!azure.loadUserTokenCache()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_main);
            //BackgroundServices.scheduleJobCheckEmergency(this);
            location = new Location(this);
            location.setup(this);
            try {
                URI uri = new URI("https://www.google.com");
                azure.insertData(new Data(StaticData.id, StaticData.name, uri, 0.0, 0.0, 2.0, Calendar.getInstance().getTime()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            if (!azure.loadData()) {
                Toast.makeText(this, "Error loading dataset", Toast.LENGTH_SHORT).show();
            } else {
                BackgroundServices.scheduleJobCheckEmergency(this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == LOCATION_SERVICE_REQUEST) {
            location.setup(this);
        }
    }

}