package be.iminds.mix.streamstore;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

/**
 * Created by matthias on 30/08/13.
 * yay
 */
public class BatteryState {
    Context context;
    String batteryState = "";
    boolean isCharging = false;
    float batteryPct = 0.0f;
    public BatteryState(Context context) {
        this.context = context;
    }

    public void setBatteryState(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        batteryPct = level / (float)scale;
    }

    public String toString(){
        setBatteryState();
        batteryState = "\"battery\":{\"charging\":" + isCharging + ", \"level\":" + batteryPct + "}";
        return batteryState;
    }
}
