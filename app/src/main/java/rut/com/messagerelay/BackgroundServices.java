package rut.com.messagerelay;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.location.LocationManager;

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
        builder.setMinimumLatency(3600000);
        builder.setOverrideDeadline(3 * 60 * 60 * 1000);

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
        builder.setOverrideDeadline(3 * 60 * 1000);    //TODO: Find optimum updatedAt for this

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

    public class CheckEmergencyJob extends JobService {

        @Override
        public boolean onStartJob(JobParameters params) {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            android.location.Location location = null;
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            Data data = new Data(StaticData.id, location.getLatitude(), location.getLongitude(), location.getAccuracy(), Calendar.getInstance().getTime());
            if (StaticData.userData.containsKey(StaticData.id)) {
                StaticData.userData.remove(StaticData.id);
            }
            StaticData.userData.put(StaticData.id, data);
            Azure azure = new Azure(this);
            azure.connect();
            azure.updateData(data);

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
                    scheduleJobEmergencySituation(this);
                }
            }
            return true;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            return false;
        }
    }

    public class EmergencySituationJob extends JobService {

        @Override
        public boolean onStartJob(JobParameters params) {
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

