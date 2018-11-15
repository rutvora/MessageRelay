package rut.com.messagerelay.UserData;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class DataManipulator {

    public String getString() {
        JSONObject mainObj = new JSONObject();
        try {
            for (String key : StaticData.userData.keySet()) {
                Data data = StaticData.userData.get(key);
                JSONObject obj = new JSONObject();
                obj.put("updatedAt", data.updatedAt);
                obj.put("id", data.id);
                obj.put("latitude", data.latitude);
                obj.put("longitude", data.longitude);
                obj.put("accuracy", data.accuracy);
                obj.put("name", data.name);
                obj.put("imageUri", data.imageUri);
                mainObj.put(key, obj);

            }
            return mainObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HashMap<String, Data> getHashMap(String json) {
        HashMap<String, Data> hashMap = new HashMap<>();
        try {
            JSONObject mainObj = new JSONObject(json);
            for (Iterator<String> it = mainObj.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject obj = mainObj.getJSONObject(key);
                Data data = new Data(obj.getString("id"),
                        obj.getString("name"),
                        new URI(obj.getString("imageURI")),
                        Double.parseDouble(obj.getString("latitude")),
                        Double.parseDouble(obj.getString("longitude")),
                        Double.parseDouble(obj.getString("accuracy")),
                        (Date) obj.get("updatedAt"));
                hashMap.put(key, data);
            }
            return hashMap;
        } catch (JSONException | URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

    }

}
