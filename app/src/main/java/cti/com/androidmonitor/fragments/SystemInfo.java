package cti.com.androidmonitor.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cti.com.androidmonitor.R;
import cti.com.androidmonitor.hardware.Battery;

import static android.os.BatteryManager.BATTERY_PLUGGED_AC;
import static android.os.BatteryManager.BATTERY_PLUGGED_USB;

public class SystemInfo extends Fragment {

    TextView text1, text2, kernel , textHardware, cpuCores, cpuArch, maxFreq, minFreq,
    battery, batVolt, batteryStatus, batteryLvl, batteryTemp;

    String kernelExe[] = {"uname", "-r", "-v"};
    String kernelExe2[] = {"cat", "/proc/version"};

    File maxFreqFile = new File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
    File minFreqFile = new File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq");

    String cpuMaxFreq[];
    String cpuMinFreq[];

    private String TAG_CPU = "cpu", TAG_KERNEL = "kernel", TAG_CORES = "cores",
    TAG_MIN_FREQ = "MIN_FREQ", TAG_MAX_FREQ = "MAX_FREQ", TAG_VOLTAGE = "voltage";

    private GetInfoOS threadInfo;

    private Battery batteryMy;

    ScaleBattery register;

    public class ScaleBattery extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            if (isCharging) {
                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                boolean usbCharge = chargePlug == BATTERY_PLUGGED_USB;
                boolean acCharge = chargePlug == BATTERY_PLUGGED_AC;
                if (usbCharge)
                    batteryStatus.setText("Charging by USB");
                else if (acCharge)
                    batteryStatus.setText("Charging by AC");
            } else {
                batteryStatus.setText("Discharging");
            }
            batVolt.setText(batteryMy.getVoltage(intent));
            battery.setText(batteryMy.getHealthBattery(intent));
            batteryLvl.setText(String.format("%d %%", batteryMy.getBatteryCharge(intent)));
            batteryTemp.setText(String.format("%.2f %cC", batteryMy.getBatteryTemperature(intent), 0x00B0));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.systeminfo_fragment, container, false);
        text1 = (TextView) view.findViewById(R.id.osInfo_textView1);
        text2 = (TextView) view.findViewById(R.id.osInfo_textView2);
        kernel = (TextView) view.findViewById(R.id.osInfo_textView3);
        cpuCores = (TextView) view.findViewById(R.id.procCores_textView1);
        cpuArch = (TextView) view.findViewById(R.id.procArch_textView1);
        minFreq = (TextView) view.findViewById(R.id.procMinFreq_textView1);
        maxFreq = (TextView) view.findViewById(R.id.procMaxFreq_textView1);
        battery = (TextView) view.findViewById(R.id.battery_textView);
        batVolt = (TextView) view.findViewById(R.id.batteryVoltage_textView);
        batteryStatus = (TextView) view.findViewById(R.id.batteryStatus_textView);
        batteryLvl = (TextView) view.findViewById(R.id.batteryLevel_textView);
        batteryTemp = (TextView) view.findViewById(R.id.batteryTemperature_textView);
        if (maxFreqFile.exists()) {
            cpuMaxFreq = new String[] {"cat", maxFreqFile.getAbsolutePath()};
        } else {
            maxFreq.setVisibility(View.GONE);
            view.findViewById(R.id.procMaxFreq_textView0).setVisibility(View.GONE);
        }
        if (minFreqFile.exists()) {
            cpuMinFreq = new String[] {"cat", minFreqFile.getAbsolutePath()};
        } else {
            minFreq.setVisibility(View.GONE);
            view.findViewById(R.id.procMinFreq_textView0).setVisibility(View.GONE);
        }
        //textView[1] = (TextView) view.findViewById(R.id.procInfo_textView1);
        text1.setText("Android "+Build.VERSION.RELEASE);
        if (Build.VERSION.SDK_INT >= 23) {
            text2.setText(Build.VERSION.SECURITY_PATCH);
        } else {
            text2.setVisibility(View.GONE);
            view.findViewById(R.id.osInfo_patch).setVisibility(View.GONE);
        }
        textHardware = (TextView) view.findViewById(R.id.procInfo_textView1);
        cpuArch.setText(System.getProperty("os.arch"));
        register = new ScaleBattery();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        batteryMy = new Battery(getActivity(), register);
        threadInfo = new GetInfoOS();
        threadInfo.onStart();
        /*batteryInfo = new BatteryInfo();
        batteryInfo.onStart();*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        batteryMy.unregister(getActivity());
    }

    private class GetInfoOS implements Runnable {

        Thread t;

        void onStart() {
            t = new Thread(this);
            t.start();
        }

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle data = msg.getData();
                textHardware.setText(data.getString(TAG_CPU));
                kernel.setText(data.getString(TAG_KERNEL));
                cpuCores.setText(data.getString(TAG_CORES));
                minFreq.setText(data.getString(TAG_MIN_FREQ, ""));
                maxFreq.setText(data.getString(TAG_MAX_FREQ, ""));
            }
        };

        @Override
        public void run() {
            String str = getProcName(executeCMD("cat", "/proc/cpuinfo"));
            Bundle bundle = new Bundle();
            bundle.putString(TAG_CPU, str);
            str = executeCMD(kernelExe);
            bundle.putString(TAG_KERNEL, str);
            if (cpuMinFreq != null) {
                str = executeCMD(cpuMinFreq);
                if (str != null && str.length() > 0) {
                    str = getHz(Float.parseFloat(str.trim()));
                    bundle.putString(TAG_MIN_FREQ, str);
                }
            }

            if (cpuMaxFreq != null) {
                str = executeCMD(cpuMaxFreq);
                if (str != null && str.length() > 0) {
                    str = getHz(Float.parseFloat(str.trim()));
                    bundle.putString(TAG_MAX_FREQ, str.trim());
                }
            }
            bundle.putString(TAG_CORES, String.valueOf(Runtime.getRuntime().availableProcessors()));
            Message message = new Message();
            message.setData(bundle);
            handler.sendMessage(message);
        }

        String executeCMD(String ... cmds) {
            ProcessBuilder pb = new ProcessBuilder(cmds);
            try {
                Process p = pb.start();
                p.waitFor();
                InputStream in = p.getInputStream();
                byte[] buff = new byte[128];
                int read;
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                while ((read = in.read(buff, 0, 128)) > 0) {
                    bout.write(buff, 0, read);
                }
                in.close();
                String result = new String(bout.toByteArray());
                bout.close();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Error";
        }

        String getProcName(String str) {
            String[] cpu = str.split(" +|\\n");
            StringBuilder sb = new StringBuilder();
            sb.append(cpu[1]).append(" ").append(cpu[2]).append(" ").append(cpu[3]).append(" ").append(cpu[4]).
            append(" ").append(cpu[5]);
            return sb.toString();
        }

        String getHz(float hz) {
            if (hz > 1000.F) {
                hz /= 1000.F;
                if (hz > 1000.F) {
                    hz /= 1000.F;
                    return String.format("%.2f GHz", hz);
                }
                return String.format("%.2f MHz", hz);
            } else {
                return String.format("%f Hz", hz);
            }
        }

        /*public String getCpuName() {
            try {
                FileReader fr = new FileReader("/proc/cpuinfo");
                BufferedReader br = new BufferedReader(fr);
                String text = br.readLine();
                br.close();
                String[] array = text.split(":\\s+", 2);
                if (array.length >= 2) {
                    return array[1];
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }*/
    }
}
