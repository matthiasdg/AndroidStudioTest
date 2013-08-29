package be.iminds.mix.streamstore;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.webkit.JavascriptInterface;


/**
 * Created by matthias on 30/05/13. gathers sensor data
 */
public class SensorData {
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    String sensorData;
    Location myLocation;
    float[] linear_acceleration;
    float light;
    // this can be called from javascript
    @JavascriptInterface
    public String toString(){
        String accelString = "{\"x\":"+linear_acceleration[0]+",\"y\":"+linear_acceleration[1]+",\"z\":"+linear_acceleration[2]+"}";
        if(myLocation != null){
            sensorData =
                    "\"location\":{\"provider\":\""+myLocation.getProvider()+"\","+
                                "\"time\":" + myLocation.getTime()+","+
                                "\"latitude\":" + myLocation.getLatitude()+","+
                                "\"longitude\":" +myLocation.getLongitude()+","+
                                "\"hasAltitude\":" + myLocation.hasAltitude()+","+
                                "\"altitude\":" + myLocation.getAltitude()+","+
                                "\"hasSpeed\":" + myLocation.hasSpeed()+","+
                                "\"speed\":" + myLocation.getSpeed()+","+
                                "\"hasBearing\":" + myLocation.hasBearing()+","+
                                "\"bearing\":" + myLocation.getBearing()+ ","+
                                "\"hasAccuracy\":"+ myLocation.hasAccuracy()+","+
                                "\"accuracy\":"+ myLocation.getAccuracy()+ "},";
        }
        else{
            sensorData = "";
        }
        sensorData +=
                "\"acceleration\":"+ accelString+ ","+
                "\"light\":"+ light;
        return sensorData;
    }

    public SensorData(Context ctx){
        // Acquire a reference to the system Location Manager
        linear_acceleration = new float[3];
        final float[] gravity = new float[3];
        LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        SensorManager sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        Sensor acSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        SensorEventListener lightListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                light = event.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        SensorEventListener acListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // In this example, alpha is calculated as t / (t + dT),
                // where t is the low-pass filter's time-constant and
                // dT is the event delivery rate.

                final float alpha = 0.8f;

                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                // Remove the gravity contribution with the high-pass filter.
                linear_acceleration[0] = event.values[0] - gravity[0];
                linear_acceleration[1] = event.values[1] - gravity[1];
                linear_acceleration[2] = event.values[2] - gravity[2];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
//        delay in MICROseconds
        sensorManager.registerListener(acListener, acSensor, 500000);
        sensorManager.registerListener(lightListener, lightSensor, 5000000);

// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the location provider.
                if(isBetterLocation(location, myLocation)){
                    myLocation = location;
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };

// Register the listener with the Location Manager to receive location updates
//        om de 300000 ms of 5 minuten checken
//        Log.d("LOCATION", String.valueOf(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000, 0, locationListener);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 0, locationListener);
        myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }
    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */

//    2 minutes maar laten staan, aangezien we zowel gps als netwerk gebruiken
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
