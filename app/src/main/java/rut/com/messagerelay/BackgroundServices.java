package rut.com.messagerelay;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import rut.com.messagerelay.UserData.Location;
import rut.com.messagerelay.WiFiDirect.WiFiDirectService;

public class BackgroundServices {
    static void scheduleJobCheckEmergency(AppCompatActivity activity) {
        ComponentName serviceComponent = new ComponentName(activity, CheckEmergencyJob.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(3600000);
        builder.setOverrideDeadline(3 * 60 * 60 * 1000);

        JobScheduler jobScheduler;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jobScheduler = activity.getSystemService(JobScheduler.class);
        } else {
            jobScheduler = (JobScheduler) activity.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
        if (jobScheduler != null) {
            jobScheduler.schedule(builder.build());
        }
    }

    static void scheduleJobEmergencySituation(AppCompatActivity activity) {
        ComponentName serviceComponent = new ComponentName(activity, EmergencySituationJob.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(3600000);                 //TODO: Find optimum updatedAt for this
        builder.setOverrideDeadline(3 * 60 * 60 * 1000);    //TODO: Find optimum updatedAt for this

        JobScheduler jobScheduler;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jobScheduler = activity.getSystemService(JobScheduler.class);
        } else {
            jobScheduler = (JobScheduler) activity.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
        if (jobScheduler != null) {
            jobScheduler.schedule(builder.build());
        }
    }

    public class CheckEmergencyJob extends JobService {

        @Override
        public boolean onStartJob(JobParameters params) {
            //TODO: Make API call to check emergency
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
            //TODO: Start relay (include SOS in case of option activated)
            //Setup WiFiDirect
            WiFiDirectService wiFiDirectService = new WiFiDirectService(this);
            wiFiDirectService.setup();

            //Setup fetching location
            Location location = new Location(this);
            location.setup(null);
            return true;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            return false;
        }
    }
}

