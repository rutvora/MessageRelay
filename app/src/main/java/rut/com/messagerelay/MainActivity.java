package rut.com.messagerelay;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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

        //check once
        setContentView(R.layout.activity_main);

        final EditText editTextFullname = findViewById(R.id.Name);
        final Button button7 = findViewById(R.id.save);

        editTextFullname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String FullnameInput = editTextFullname.getText().toString();

                button7.setEnabled(!FullnameInput.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        Button button1 = findViewById(R.id.first);
        Button button2 = findViewById(R.id.existing);
        ImageButton button3 = findViewById(R.id.facebook);
        ImageButton button4 = findViewById(R.id.google);
        Button button5 = findViewById(R.id.SOS);
        Button button6 = findViewById(R.id.image);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == LOCATION_SERVICE_REQUEST) {
            location.setup();
        }
    }

}