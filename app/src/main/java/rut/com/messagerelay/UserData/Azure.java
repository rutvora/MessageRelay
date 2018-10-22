package rut.com.messagerelay.UserData;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import java.net.MalformedURLException;

public class Azure {

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

    public void test() {
        TodoItem item = new TodoItem();
        item.text = "Awesome item";
        item.id = "Some ID";
        ListenableFuture<TodoItem> listenableFuture = mClient.getTable(TodoItem.class).insert(item);
        listenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                Log.d("Azure", "Update Successful");
            }
        }, MoreExecutors.directExecutor());
    }

    class TodoItem {
        String id;
        String text;
    }
}
