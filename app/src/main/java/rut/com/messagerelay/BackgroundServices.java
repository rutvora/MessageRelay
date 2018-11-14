package rut.com.messagerelay;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import rut.com.messagerelay.UserData.Azure;
import rut.com.messagerelay.UserData.Data;
import rut.com.messagerelay.UserData.EmergencyTable;
import rut.com.messagerelay.UserData.Location;
import rut.com.messagerelay.UserData.StaticData;
import rut.com.messagerelay.WiFiDirect.WiFiDirectService;

public class BackgroundServices {
    static void scheduleJobCheckEmergency(Context context) {
        ComponentName serviceComponent = new ComponentName(context, CheckEmergencyJob.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(60 * 60 * 1000);
        builder.setOverrideDeadline(3 * 60 * 60 * 1000);
//        builder.setMinimumLatency(1 * 1000);
//        builder.setOverrideDeadline(2 * 1000);

        JobScheduler jobScheduler;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jobScheduler = context.getSystemService(JobScheduler.class);
        } else {
            jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
        if (jobScheduler != null) {
            for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
                if (jobInfo.getId() == 0) {
                    return;
                }
            }
            jobScheduler.schedule(builder.build());
        }
    }

    private static void scheduleJobEmergencySituation(Context context) {
        ComponentName serviceComponent = new ComponentName(context, EmergencySituationJob.class);
        JobInfo.Builder builder = new JobInfo.Builder(1, serviceComponent);
        builder.setMinimumLatency(2 * 60 * 1000);                 //TODO: Find optimum updatedAt for this
        builder.setOverrideDeadline(5 * 60 * 1000);    //TODO: Find optimum updatedAt for this

        JobScheduler jobScheduler;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jobScheduler = context.getSystemService(JobScheduler.class);
        } else {
            jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
        if (jobScheduler != null) {
            for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
                if (jobInfo.getId() == 1) {
                    return;
                }
            }
            jobScheduler.schedule(builder.build());
        }
    }

    public static class CheckEmergencyJob extends JobService {

        @SuppressLint("MissingPermission")
        @Override
        public boolean onStartJob(JobParameters params) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Log.d("CheckEmergencyJob", "Start");
                    LocationManager locationManager = (LocationManager) CheckEmergencyJob.this.getSystemService(Context.LOCATION_SERVICE);
                    android.location.Location location = null;
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    Data data = null;
                    if (location != null)
                        data = new Data(StaticData.id, StaticData.name, StaticData.imageUri, location.getLatitude(), location.getLongitude(), location.getAccuracy(), Calendar.getInstance().getTime());

                    if (StaticData.userData.containsKey(StaticData.id)) {
                        StaticData.userData.remove(StaticData.id);
                    }
                    StaticData.userData.put(StaticData.id, data);
                    Azure azure = new Azure(CheckEmergencyJob.this);
                    azure.setup();
                    azure.setupSync();
                    if (data != null) azure.updateData(data);

                    Log.d("CheckEmergencyJob", "Data updated");

                    List<EmergencyTable> emergencyZones = azure.getEmergencyZones();
                    float[] results = new float[3];
                    for (EmergencyTable table : emergencyZones) {
                        android.location.Location.distanceBetween(
                                Double.parseDouble(table.latitude),
                                Double.parseDouble(table.longitude),
                                Double.parseDouble(StaticData.userData.get(StaticData.id).latitude),
                                Double.parseDouble(StaticData.userData.get(StaticData.id).longitude),
                                results);
                        if (results[0] < Double.parseDouble(table.radius)) {
                            scheduleJobEmergencySituation(CheckEmergencyJob.this);
                        }
                        Log.d("CheckEmergencyJob", "End");
                    }
                }
            };
            thread.start();

            return true;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            return false;
        }
    }

    public static class EmergencySituationJob extends JobService {

        @Override
        public boolean onStartJob(JobParameters params) {
            Log.d("EmergencySituationJob", "Ran");
            //TODO: Start relay
            //Setup WiFiDirect
            WiFiDirectService wiFiDirectService = new WiFiDirectService(this);
            wiFiDirectService.setup();

            //Setup fetching location
            Location location = new Location(this);                 //TODO: check if this schedules location updates multiple times
            location.setup(null);
            return true;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            return false;
        }
    }
}

