package rut.com.messagerelay.UserData;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.MobileServicePreconditionFailedExceptionJson;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.operations.RemoteTableOperationProcessor;
import com.microsoft.windowsazure.mobileservices.table.sync.operations.TableOperation;
import com.microsoft.windowsazure.mobileservices.table.sync.push.MobileServicePushCompletionResult;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.MobileServiceSyncHandler;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.MobileServiceSyncHandlerException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import rut.com.messagerelay.MainActivity;

public class Azure {

    private static final String SHAREDPREFFILE = "tokenCache";
    private static final String USERIDPREF = "uid";
    private static final String TOKENPREF = "token";
    private static final String NAMEPREF = "name";
    private static final String IMAGEPREF = "imageUri";
    private static final String storageContainer = "messagerelay";
    private static final String storageConnectionString
            = "DefaultEndpointsProtocol=https;AccountName=usercontents;AccountKey=WpPxpNXcNBKMkL7TNjbN0thJvFDNI2ohVkPL73jrfbmXlbsL4VKzuuyxEDLjHj1hqDnm8sBU/omwJnN249TXeg==;EndpointSuffix=core.windows.net";
    private MobileServiceSyncTable<Data> userDataTable;

    public MobileServiceClient mobileServiceClient;
    private Context context;
    private MobileServiceSyncContext syncContext;

    public Azure(Context context) {
        this.context = context;
    }


    private void cacheUserToken(MobileServiceUser user) {
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USERIDPREF, user.getUserId());
        editor.putString(TOKENPREF, user.getAuthenticationToken());
        editor.putString(NAMEPREF, StaticData.name);
        editor.putString(IMAGEPREF, StaticData.imageUri.toString());
        editor.apply();

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            @SuppressLint("MissingPermission")
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Data data;
            if (location != null)
                data = new Data(StaticData.id, StaticData.name, StaticData.imageUri, location.getLatitude(), location.getLongitude(), location.getAccuracy(), Calendar.getInstance().getTime());
            else
                data = new Data(StaticData.id, StaticData.name, StaticData.imageUri, 0.0, 0.0, 0.0, Calendar.getInstance().getTime());
            StaticData.userData.put(StaticData.id, data);
        }


        loadUserTokenCache();
    }

    public boolean loadUserTokenCache() {
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);

        String userId = prefs.getString(USERIDPREF, null);
        if (userId == null)
            return false;

        String token = prefs.getString(TOKENPREF, null);
        if (token == null)
            return false;

        String imageUri = prefs.getString(IMAGEPREF, null);
        if (imageUri == null)
            return false;

        String name = prefs.getString(NAMEPREF, null);
        if (name == null)
            return false;


        StaticData.id = userId;
        StaticData.name = name;
        try {
            StaticData.imageUri = new URI(imageUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        MobileServiceUser user = new MobileServiceUser(userId);
        user.setAuthenticationToken(token);
        mobileServiceClient.setCurrentUser(user);

        return true;
    }

    public void setup() {
        try {
            mobileServiceClient = new MobileServiceClient("https://messagerelay.azurewebsites.net", context);
            loadUserTokenCache();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void setupSync() {
        userDataTable = mobileServiceClient.getSyncTable("userData", Data.class);
        try {

            SQLiteLocalStore localStore = new SQLiteLocalStore(mobileServiceClient.getContext(), "userData", null, 1);
            MobileServiceSyncHandler handler = new ConflictResolvingSyncHandler();

            Map<String, ColumnDataType> tableDefinition = new HashMap<>();
            tableDefinition.put("id", ColumnDataType.String);
            tableDefinition.put("latitude", ColumnDataType.String);
            tableDefinition.put("longitude", ColumnDataType.String);
            tableDefinition.put("accuracy", ColumnDataType.String);
            tableDefinition.put("name", ColumnDataType.String);
            tableDefinition.put("imageUri", ColumnDataType.String);
            tableDefinition.put("updatedAt", ColumnDataType.Date);

            localStore.defineTable("userData", tableDefinition);
            syncContext = mobileServiceClient.getSyncContext();

            syncContext.initialize(localStore, handler).get();

            sync();
        } catch (InterruptedException | ExecutionException | MobileServiceLocalStoreException e) {
            e.printStackTrace();
        }
    }

    public void storeImageInBlobStorage(final Uri imageuri, final Activity loginActivity) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    // Retrieve storage account from connection-string.
                    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

                    // Create the blob client.
                    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

                    // Retrieve reference to a previously created container.
                    CloudBlobContainer container = blobClient.getContainerReference(storageContainer);
                    container.createIfNotExists();

                    // Create or overwrite the blob (with the name "example.jpeg") with contents from a local file.
                    CloudBlockBlob blob = container.getBlockBlobReference(StaticData.id);
                    InputStream imageStream = Objects.requireNonNull(context.getContentResolver().openInputStream(imageuri));
                    long length = 0;
                    while (imageStream.available() > 0) {
                        length += imageStream.available();
                        imageStream.read(new byte[imageStream.available()]);
                    }
                    blob.upload(Objects.requireNonNull(context.getContentResolver().openInputStream(imageuri)), length);
                    StaticData.imageUri = blob.getUri();
                    cacheUserToken(mobileServiceClient.getCurrentUser());

                    Intent intent = new Intent(loginActivity, MainActivity.class);
                    loginActivity.startActivity(intent);
                    loginActivity.finish();

                } catch (StorageException | IOException | InvalidKeyException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();

    }

    public List<EmergencyTable> getEmergencyZones() {
        MobileServiceTable<EmergencyTable> table = mobileServiceClient.getTable("emergency", EmergencyTable.class);
        List<EmergencyTable> results = null;
        try {
            results = table.execute().get();
        } catch (InterruptedException | ExecutionException | MobileServiceException e) {
            e.printStackTrace();
        }
        return results;
    }

    public void insertData(Data data) {
        userDataTable.insert(data);
        sync();

    }

    public void updateData(Data data) {
        userDataTable.update(data);
        sync();

    }

    public boolean loadData() {                 //TODO: call and load Data from this
        List<Data> results;
        try {
            if (userDataTable != null) {
                Log.d("loadData", "Not Null table " + userDataTable.getName());
                results = userDataTable.read(null).get();   //QueryOperations.add().orderBy("id", QueryOrder.Ascending)
                Log.d("Shit debugger", results.size() + "");

                for (Data result : results) {
                    Log.d("Data", result.id + result.name);
                    StaticData.userData.put(result.id, result);
                }
            }
            return true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sync() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Log.d("Sync", "Starting Push");
                    ListenableFuture<Void> future = syncContext.push();
                    Futures.addCallback(future, new FutureCallback<Void>() {
                        @Override
                        public void onSuccess(@Nullable Void result) {
                            Log.d("Sync", "Pushed");
                        }

                        @Override
                        public void onFailure(@NonNull Throwable t) {
                            Log.d("Sync", "Error in pushing");
                            t.printStackTrace();
                        }
                    });
                    future.get();
                    Log.d("Sync", "Starting pull");
                    future = userDataTable.pull(null);
                    Futures.addCallback(future, new FutureCallback<Void>() {
                        @Override
                        public void onSuccess(@Nullable Void result) {
                            Log.d("Sync", "Pulled");
                        }

                        @Override
                        public void onFailure(@NonNull Throwable t) {
                            Log.d("Sync", "Error in pulling");
                            t.printStackTrace();
                        }
                    });
                    Log.d("Sync", "Sync completed");
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        task.execute();
    }

    private class ConflictResolvingSyncHandler implements MobileServiceSyncHandler {

        @Override
        public JsonObject executeTableOperation(
                RemoteTableOperationProcessor processor, TableOperation operation)
                throws MobileServiceSyncHandlerException {

            MobileServicePreconditionFailedExceptionJson ex = null;
            JsonObject result = null;
            try {
                result = operation.accept(processor);
            } catch (MobileServicePreconditionFailedExceptionJson e) {
                ex = e;
            } catch (Throwable e) {
                ex = (MobileServicePreconditionFailedExceptionJson) e.getCause();
            }

            if (ex != null) {
                // A conflict was detected; let's force the server to "win"
                // by discarding the client version of the item
                // Other policies could be used, such as prompt the user for
                // which version to maintain.
                JsonObject serverItem = ex.getValue();

                if (serverItem == null) {
                    // Item not returned in the exception, retrieving it from the server
                    try {
                        serverItem = mobileServiceClient.getSyncTable(operation.getTableName()).lookUp(operation.getItemId()).get();
                    } catch (Exception e) {
                        throw new MobileServiceSyncHandlerException(e);
                    }
                }

                result = serverItem;
            }

            return result;
        }

        @Override
        public void onPushComplete(MobileServicePushCompletionResult result) {
        }
    }
}