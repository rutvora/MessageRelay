package rut.com.messagerelay.UserData;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;

public class Data implements Serializable {

    public Date updatedAt;
    //TODO: Name and Picture
    public String id;
    public String latitude;
    public String longitude;
    public String accuracy;
    public String name;
    String imageUri;

    public Data(String id, String name, URI imageUri, double latitude, double longitude, double accuracy, Date updatedAt) {
        this.id = id;
        this.latitude = latitude + "";
        this.longitude = longitude + "";
        this.accuracy = accuracy + "";
        this.updatedAt = updatedAt;
        this.name = name;
        this.imageUri = imageUri.toString();
    }

}

