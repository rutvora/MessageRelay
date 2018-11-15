package rut.com.messagerelay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.myhexaville.smartimagepicker.ImagePicker;
import com.myhexaville.smartimagepicker.OnImagePickedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.Arrays;

import rut.com.messagerelay.UserData.Azure;
import rut.com.messagerelay.UserData.Location;
import rut.com.messagerelay.UserData.StaticData;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, OnImagePickedListener {

    Azure azure;

    ImagePicker imagePicker;
    CallbackManager callbackManager;
    private EditText userName;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        ImageButton facebook = findViewById(R.id.facebook);
        facebook.setOnClickListener(this);
        ImageButton google = findViewById(R.id.google);
        google.setOnClickListener(this);
        azure = new Azure(this);
        azure.setup();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (callbackManager != null)
            callbackManager.onActivityResult(requestCode, resultCode, data);
        if (imagePicker != null)
            imagePicker.handleActivityResult(resultCode, requestCode, data);
        if (requestCode == 3) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    Log.d("IdToken", account.getIdToken());
                    azureLogin(account.getIdToken(), MobileServiceAuthenticationProvider.Google);
                }
                // Signed in successfully, show authenticated UI.

            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w("Google Sign In", "signInResult:failed code=" + e.getMessage());
                e.printStackTrace();

            }
        }

    }

    private void googleSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("336884445870-d797qo5pplss3o4ujprm5a11d3dgi7ir.apps.googleusercontent.com")
                    .requestEmail()
                    .build();

            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            signIn(mGoogleSignInClient);
        }

    }

    private void signIn(GoogleSignInClient mGoogleSignInClient) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 3);
    }

    private void loginWithFacebook() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.d("Facebook Login", "Success " + loginResult.getAccessToken().getToken());
                        findViewById(R.id.loading).setVisibility(View.VISIBLE);
                        azureLogin(loginResult.getAccessToken().getToken(), MobileServiceAuthenticationProvider.Facebook);
                    }

                    @Override
                    public void onCancel() {
                        Log.d("Login", "Cancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.d("Login", "error");
                        exception.printStackTrace();
                    }
                });
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
        Log.d("Facebook auth", "Step 1");
    }

    private void changeContent() {
        setContentView(R.layout.login);
        ImageView userIcon = findViewById(R.id.photo);
        userIcon.setOnClickListener(this);
        userName = findViewById(R.id.Name);
        Button save = findViewById(R.id.save);
        save.setOnClickListener(this);
    }

    private void azureLogin(String accessToken, MobileServiceAuthenticationProvider provider) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("access_token", accessToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            azure.mobileServiceClient = new MobileServiceClient("https://messagerelay.azurewebsites.net", LoginActivity.this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        ListenableFuture<MobileServiceUser> mLogin = azure.mobileServiceClient.login(provider, payload.toString());
        Futures.addCallback(mLogin, new FutureCallback<MobileServiceUser>() {
            @Override
            public void onFailure(@NonNull Throwable exc) {
                exc.printStackTrace();
            }

            @Override
            public void onSuccess(MobileServiceUser user) {
                Log.d("Azure login", "Login Complete");
                changeContent();
                StaticData.id = user.getUserId();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.facebook) {
            loginWithFacebook();
        } else if (v.getId() == R.id.google) {
            Toast.makeText(this, "Only facebook supported for demo purposes", Toast.LENGTH_SHORT).show();
            //googleSignIn();
        } else if (v.getId() == R.id.photo) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                imagePicker = new ImagePicker(this, null, this);
                imagePicker.choosePicture(true);
            }
        } else if (v.getId() == R.id.save) {
            String name = userName.getText().toString();
            if (name.equals("")) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (imageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
                return;
            }
            StaticData.name = name;
            findViewById(R.id.loading).setVisibility(View.VISIBLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.LOCATION_PERMISSION_REQUEST);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            imagePicker = new ImagePicker(this, null, this);
            imagePicker.choosePicture(true);
        }
        if (requestCode == MainActivity.LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new Location(this).setup(this);
            azure.storeImageInBlobStorage(imageUri, this);
        }
    }

    @Override
    public void onImagePicked(Uri imageUri) {
        this.imageUri = imageUri;
        ImageView pic = findViewById(R.id.photo);
        pic.setImageURI(imageUri);
        Log.d("Image Uri", imageUri.toString());
    }


}