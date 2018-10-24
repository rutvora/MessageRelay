package rut.com.messagerelay.UserData;

import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import rut.com.messagerelay.MainActivity;

public class DataManipulator {

    public File getFile() {
        File locationData;
        if (Environment.getDataDirectory().exists()) {
            locationData = new File(Environment.getDataDirectory(), "locationData");

        } else {
            locationData = null;
        }
        return locationData;
    }

    public byte[] getByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(MainActivity.userData);
            out.flush();
            byte[] yourBytes = bos.toByteArray();
            return yourBytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    public HashMap<String, Data> getHashMap(byte[] array) {
        ByteArrayInputStream bis = new ByteArrayInputStream(array);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            HashMap<String, Data> hashMap = (HashMap<String, Data>) in.readObject();
            return hashMap;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

}
