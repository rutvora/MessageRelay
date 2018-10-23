package rut.com.messagerelay.UserData;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StorageFile {

    private AppCompatActivity activity;

    public StorageFile(AppCompatActivity activity) {
        this.activity = activity;
    }

    public File getFile() {
        File locationData;
        if (Environment.getDataDirectory().exists()) {
            locationData = new File(Environment.getDataDirectory(), "locationData");

        } else {
            locationData = null;
        }
        return locationData;
    }

    public void readFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[10];
            StringBuilder sb = new StringBuilder();
            while (fis.read(buffer) != -1) {
                sb.append(new String(buffer));
                buffer = new byte[10];
            }
            fis.close();

            String content = sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Map<String, Data> parseData(String s) {
        String array[] = s.split("\n");
        HashMap<String, Data> map = new HashMap<>();
        for (String line : array) {
            String parsedContent[] = line.split(",");
            map.put(parsedContent[0], new Data(parsedContent[0], parsedContent[1], parsedContent[2], Long.parseLong(parsedContent[3])));
        }
        return map;
    }

    public void updateData(String uid, Data data, Map<String, Data> map) {
        if (map.containsKey(uid)) {
            Data d = map.get(uid);
            if (d.time < data.time) {
                map.remove(uid);
                map.put(uid, data);
            }
        } else {
            map.put(uid, data);
        }
    }

    public void writeToFile(Data data, File file) {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(data.getString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
