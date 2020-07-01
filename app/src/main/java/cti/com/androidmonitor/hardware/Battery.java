package cti.com.androidmonitor.hardware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class Battery {

    BroadcastReceiver broadcast;

    public Battery(Context activity, BroadcastReceiver broadcast) {
        this.broadcast = broadcast;
        activity.registerReceiver(broadcast, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public void unregister(Context activity) {
        activity.unregisterReceiver(broadcast);
    }

    public int getBatteryCharge(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return level*100/scale;

    }

    public float getBatteryTemperature(Intent intent) {
        int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        return (float) temp / 10f;
    }

    public int getBatteryStatus(Intent intent) {
        return intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
    }

    public String getVoltage(Intent intent) {
        return String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0));
    }

    public String getHealthBattery(Intent intent) {
        switch (getBatteryStatus(intent)) {
            case BatteryManager.BATTERY_HEALTH_GOOD: return "Good";
            case BatteryManager.BATTERY_HEALTH_DEAD: return "Dead";
            case BatteryManager.BATTERY_HEALTH_COLD: return "Cold";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: return "Over voltage";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT: return "Overheat";
            default: return "Unknown";
        }
    }

}
