package rut.com.messagerelay.UserData;

import java.io.Serializable;
import java.util.Date;

public class Data implements Serializable {
    public Date updatedAt;
    //TODO: Name and Picture
    private String id;
    private String latitude;
    private String longitude;
    private String accuracy;

    public Data(String id, double latitude, double longitude, double accuracy, Date updatedAt) {
        this.id = id;
        this.latitude = latitude + "";
        this.longitude = longitude + "";
        this.accuracy = accuracy + "";
        this.updatedAt = updatedAt;
    }

}

