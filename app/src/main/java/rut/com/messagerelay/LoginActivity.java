package rut.com.messagerelay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.microsoft.windowsazure.mobileservices.MobileServiceActivityResult;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import rut.com.messagerelay.UserData.Azure;
import rut.com.messagerelay.UserData.StaticData;

public class LoginActivity extends AppCompatActivity {

    MobileServiceClient client;
    Azure azure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        azure = new Azure(this);
        azure.connect();
        client = azure.authenticate('g');       //TODO: Get input from user
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // When request completes
        if (resultCode == RESULT_OK) {
            // Check the request code matches the one we send in the login request
            if (requestCode == Azure.GOOGLE_LOGIN_REQUEST_CODE || requestCode == Azure.FACEBOOK_LOGIN_REQUEST_CODE) {
                MobileServiceActivityResult result = client.onActivityResult(data);
                if (result.isLoggedIn()) {
                    // sign-in succeeded
                    //createAndShowDialog(String.format("You are now signed in - %1$2s", mClient.getCurrentUser().getUserId()), "Success");
                    azure.cacheUserToken(client.getCurrentUser());
                    StaticData.id = client.getCurrentUser().getUserId();
                    Intent intent = new Intent(this, MainActivity.class);       //TODO: Maybe call updateUserProfile?
                    startActivity(intent);
                } else {
                    // sign-in failed, check the error message
                    String errorMessage = result.getErrorMessage();
                }
            }
        }

    }
}