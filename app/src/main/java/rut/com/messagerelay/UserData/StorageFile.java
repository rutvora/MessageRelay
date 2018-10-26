package rut.com.messagerelay.UserData;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StorageFile {

    public File getFile() {
        File locationData;
        if (Environment.getDataDirectory().exists()) {
            locationData = new File(Environment.getDataDirectory(), "locationData");

        } else {
            locationData = null;
        }
        return locationData;
    }

    public String readFile(File file) {
        String content = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[10];
            StringBuilder sb = new StringBuilder();
            while (fis.read(buffer) != -1) {
                sb.append(new String(buffer));
                buffer = new byte[10];
            }
            fis.close();

            content = sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
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

    public void updateData(String data) {
        Map<String, Data> receivedData = parseData(data);
        Map<String, Data> myData = parseData(readFile(getFile()));
        for (String uid : receivedData.keySet()) {
            Data d = receivedData.get(uid);
            if (myData.containsKey(uid)) {
                if (d.time > myData.get(uid).time) {
                    myData.remove(uid);
                    myData.put(uid, d);
                }
            } else {
                myData.put(uid, d);
            }
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
