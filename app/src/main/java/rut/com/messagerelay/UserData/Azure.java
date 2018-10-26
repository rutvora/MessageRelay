package rut.com.messagerelay.UserData;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOperations;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Azure {

    public static final int GOOGLE_LOGIN_REQUEST_CODE = 1;
    public static final int FACEBOOK_LOGIN_REQUEST_CODE = 2;
    private static final String SHAREDPREFFILE = "tokenCache";
    private static final String USERIDPREF = "uid";
    private static final String TOKENPREF = "token";
    private static final String storageURL = "BLOB_STORAGE_URL";
    private static final String storageContainer = "messageRelay";
    private static final String storageConnectionString
            = "DefaultEndpointsProtocol=https;AccountName=usercontents;AccountKey=Zfk4hcBWJk7W9v+THWf6LOP0OGWQy1aIvupMKiH6TR6TQsxex7ZWi3GHiHIkT11YnK5wmJWHz1SmXiN5YT7ZXQ==;EndpointSuffix=core.windows.net";
    private MobileServiceSyncTable<Data> userDataTable;
    private MobileServiceClient mClient;
    private Context context;

    public Azure(Context context) {
        this.context = context;
    }

    public MobileServiceClient authenticate(char c) {
        if (!loadUserTokenCache()) {
            if (c == 'g')   // Sign in using the Google provider.
                mClient.login(MobileServiceAuthenticationProvider.Google, "messageRelay", GOOGLE_LOGIN_REQUEST_CODE);           //TODO: check uri
            else if (c == 'f')  // Sign in using the Facebook provider.
                mClient.login(MobileServiceAuthenticationProvider.Facebook, "messageRelay", FACEBOOK_LOGIN_REQUEST_CODE);
        }

        return mClient;
    }

    /*
    public void test() {
        TodoItem item = new TodoItem();
        item.text = "Awesome item";
        item.id = "Some ID";
        userDataTable = mClient.getSyncTable("TodoItem", TodoItem.class);
        // Offline Sync

        try {
            final List<TodoItem> results = refreshItemsFromMobileServiceTableSyncTable();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }



        //ListenableFuture<TodoItem> listenableFuture =
        mClient.getSyncTable(TodoItem.class).insert(item);


        listenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                Log.d("Azure", "Update Successful");
            }
        }, MoreExecutors.directExecutor());

    }


    private List<TodoItem> refreshItemsFromMobileServiceTableSyncTable() throws ExecutionException, InterruptedException {
        //sync the data
        sync().get();
        Query query = QueryOperations.field("complete").
                eq(val(false));
        return userDataTable.read(query).get();
    }


    private static AsyncTask<Void, Void, Void> sync() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceSyncContext syncContext = mClient.getSyncContext();
                    syncContext.push().get();
                    userDataTable.pull(null).get();
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };
        return runAsyncTask(task);
    }
    */

    public void cacheUserToken(MobileServiceUser user) {
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USERIDPREF, user.getUserId());
        editor.putString(TOKENPREF, user.getAuthenticationToken());
        editor.apply();
    }

    public boolean loadUserTokenCache() {
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        String userId = prefs.getString(USERIDPREF, null);
        if (userId == null)
            return false;
        String token = prefs.getString(TOKENPREF, null);
        if (token == null)
            return false;
        StaticData.id = userId;

        MobileServiceUser user = new MobileServiceUser(userId);
        user.setAuthenticationToken(token);
        mClient.setCurrentUser(user);

        return true;
    }

    public void connect() {
        try {
            mClient = new MobileServiceClient("https://messagerelay.azurewebsites.net", context);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    protected void storeImageInBlobStorage(String imgPath) {
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

            // Create the blob client.
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

            // Retrieve reference to a previously created container.
            CloudBlobContainer container = blobClient.getContainerReference(storageContainer);

            // Create or overwrite the blob (with the name "example.jpeg") with contents from a local file.
            CloudBlockBlob blob = container.getBlockBlobReference(StaticData.id);
            File source = new File(imgPath);
            blob.upload(new FileInputStream(source), source.length());
        } catch (Exception e) {
            // Output the stack trace.
            e.printStackTrace();
        }
    }

    public List<EmergencyTable> getEmergencyZones() {
        MobileServiceTable<EmergencyTable> table = mClient.getTable("emergency", EmergencyTable.class);
        List<EmergencyTable> results = null;
        try {
            results = table.execute().get();
        } catch (InterruptedException | ExecutionException | MobileServiceException e) {
            e.printStackTrace();
        }
        return results;
    }

    public void insertData(Data data) {
        userDataTable = mClient.getSyncTable("userData", Data.class);
        userDataTable.insert(data);
    }

    public void updateData(Data data) {
        userDataTable = mClient.getSyncTable("userData", Data.class);
        userDataTable.update(data);
    }

    public boolean loadData() {                 //TODO: call and load Data from this
        MobileServiceSyncTable<Data> table = mClient.getSyncTable("userData", Data.class);
        List<Data> results;
        try {
            results = table.read(QueryOperations.add().orderBy("id", QueryOrder.Ascending)).get();
            for (Data result : results) {
                StaticData.userData.put(result.id, result);
            }
            return true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }
}