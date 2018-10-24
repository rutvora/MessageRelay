package rut.com.messagerelay.UserData;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;

import java.net.MalformedURLException;

public class Azure {

    public static final int GOOGLE_LOGIN_REQUEST_CODE = 1;
    public static final int FACEBOOK_LOGIN_REQUEST_CODE = 2;
    private static final String SHAREDPREFFILE = "tokenCache";
    private static final String USERIDPREF = "uid";
    private static final String TOKENPREF = "token";
    private MobileServiceSyncTable<TodoItem> mToDoTable;

    public void cacheUserToken(MobileServiceUser user) {
        SharedPreferences prefs = activity.getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USERIDPREF, user.getUserId());
        editor.putString(TOKENPREF, user.getAuthenticationToken());
        editor.apply();
    }

    private boolean loadUserTokenCache(MobileServiceClient client) {
        SharedPreferences prefs = activity.getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        String userId = prefs.getString(USERIDPREF, null);
        if (userId == null)
            return false;
        String token = prefs.getString(TOKENPREF, null);
        if (token == null)
            return false;

        MobileServiceUser user = new MobileServiceUser(userId);
        user.setAuthenticationToken(token);
        client.setCurrentUser(user);

        return true;
    }

    private AppCompatActivity activity;
    private MobileServiceClient mClient;

    public Azure(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void connect() {
        try {
            mClient = new MobileServiceClient("https://messagerelay.azurewebsites.net", activity);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public MobileServiceClient authenticate(char c) {
        if (!loadUserTokenCache(mClient)) {
            if (c == 'g')   // Sign in using the Google provider.
                mClient.login(MobileServiceAuthenticationProvider.Google, "", GOOGLE_LOGIN_REQUEST_CODE);
            else if (c == 'f')  // Sign in using the Facebook provider.
                mClient.login(MobileServiceAuthenticationProvider.Facebook, "", FACEBOOK_LOGIN_REQUEST_CODE);
        }

        return mClient;
    }

    public void test() {
        TodoItem item = new TodoItem();
        item.text = "Awesome item";
        item.id = "Some ID";
        mToDoTable = mClient.getSyncTable("TodoItem", TodoItem.class);
        // Offline Sync
        /*
        try {
            final List<TodoItem> results = refreshItemsFromMobileServiceTableSyncTable();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        */


        //ListenableFuture<TodoItem> listenableFuture =
        mClient.getSyncTable(TodoItem.class).insert(item);

        /*
        listenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                Log.d("Azure", "Update Successful");
            }
        }, MoreExecutors.directExecutor());
        */
    }

    /*
    private List<TodoItem> refreshItemsFromMobileServiceTableSyncTable() throws ExecutionException, InterruptedException {
        //sync the data
        sync().get();
        Query query = QueryOperations.field("complete").
                eq(val(false));
        return mToDoTable.read(query).get();
    }


    private static AsyncTask<Void, Void, Void> sync() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceSyncContext syncContext = mClient.getSyncContext();
                    syncContext.push().get();
                    mToDoTable.pull(null).get();
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };
        return runAsyncTask(task);
    }
    */

    class TodoItem {
        String id;
        String text;
    }
}