package rut.com.messagerelay.UserData;

public class Data {
    String uid;
    String latitude;
    String longitude;
    long time;

    public Data(String uid, String latitude, String longitude, long time) {
        this.uid = uid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public String getString() {
        return uid + "," + latitude + "," + longitude + "," + time;
    }

}

