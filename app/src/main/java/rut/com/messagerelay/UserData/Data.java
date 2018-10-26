package rut.com.messagerelay.UserData;

import java.io.Serializable;
import java.util.Date;

public class Data implements Serializable {
    public Date updatedAt;
    //TODO: Name and Picture
    public String id;
    public String latitude;
    public String longitude;
    public String accuracy;

    public Data(String id, double latitude, double longitude, double accuracy, Date updatedAt) {
        this.id = id;
        this.latitude = latitude + "";
        this.longitude = longitude + "";
        this.accuracy = accuracy + "";
        this.updatedAt = updatedAt;
    }

}

