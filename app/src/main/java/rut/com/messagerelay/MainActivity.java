package rut.com.messagerelay;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import rut.com.messagerelay.UserData.Azure;
import rut.com.messagerelay.UserData.Location;

public class MainActivity extends AppCompatActivity {

    public static final int LOCATION_SERVICE_REQUEST = 0;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Azure azure = new Azure(this);
        if (!azure.loadUserTokenCache()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_main);
            BackgroundServices.scheduleJobCheckEmergency(this);
            location = new Location(this);
            location.setup(this);
            if (!azure.loadData()) {
                Toast.makeText(this, "Error loading dataset", Toast.LENGTH_SHORT).show();
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