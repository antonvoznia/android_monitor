package cti.com.androidmonitor.fragments;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import cti.com.androidmonitor.R;
import cti.com.androidmonitor.hardware.Battery;
import cti.com.androidmonitor.widget.CustomProgressBar;

public class Total extends Fragment {

    TextView totalCPU, totalRAM, batteryCharge, temperatureBattery, uptime;

    ProgressBar totalCPUProgressBar, totalRAMProgressBar, totalBattery;

    CustomProgressBar cpb;

    TotalThreads t;

    Battery battery;

    BroadcastReceiver batteryReg = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int scale = battery.getBatteryCharge(intent);
            totalBattery.setProgress(scale);
            batteryCharge.setText(String.format("%d %%", scale));
            temperatureBattery.setText(String.format("Temperature %.2f %cC", battery.getBatteryTemperature(intent), 0x00B0));
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.total_fragment, container, false);
        totalCPU = (TextView) view.findViewById(R.id.totalCPU_textView);
        totalCPUProgressBar = (ProgressBar) view.findViewById(R.id.totalCPU_progressBar);
        totalRAM = (TextView) view.findViewById(R.id.totalRAM_textView);
        totalRAMProgressBar = (ProgressBar) view.findViewById(R.id.totalRAM_progressBar);
        batteryCharge = (TextView) view.findViewById(R.id.batteryCharge_textView);
        totalBattery = (ProgressBar) view.findViewById(R.id.totalBattery_progressBar);
        temperatureBattery = (TextView) view.findViewById(R.id.temperatureBattery_textView);
        uptime = (TextView) view.findViewById(R.id.uptime_textView);
        cpb = (CustomProgressBar) view.findViewById(R.id.customProgressBar);
        t = new TotalThreads();
        t.start();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        t.stop();
        battery.unregister(getActivity());
    }

    private class TotalThreads implements Runnable {

        private final String TAG_TOTAL_MEM = "TOTAL_MEM";
        private final String TAG_USAGE_MEM = "USAGE_MEM";

        private final String TAG_UPTIME = "UPTIME";

        Thread t;

        volatile int percentCpuUsage;

        volatile boolean flagThread = true;

        public void start() {
            t = new Thread(this);
            t.start();
        }

        public void stop() {
            flagThread = false;
        }

        @Override
        public void run() {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long blockSize = (long) stat.getBlockSize();
            long bytesTotal = blockSize *(long)stat.getBlockCount();
            long bytesAvailable = stat.getAvailableBlocks()*blockSize;
            float megTotal = (float) bytesTotal/1048576F;
            final String resultMemory;
            final float percentUsage =  (bytesTotal-bytesAvailable)*100/bytesTotal;
            if (megTotal > 1023) {
                megTotal/=1024F;
                float gbAvailable = bytesAvailable/1073741824F;
                resultMemory = String.format("%.2f Gb / %.2f Gb", megTotal-gbAvailable, megTotal);
            } else {
                float megAvaible = (float)bytesAvailable/1048576F;
                resultMemory = String.format("%.2f Mb / %.2f Mb", megTotal - megAvaible, megTotal);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    cpb.setText(resultMemory);
                    cpb.setProgress((int) percentUsage);
                }
            });

            battery = new Battery(getActivity(), batteryReg);

            while (flagThread) {
                percentCpuUsage = cpuUsageTotal(readCPUUsage());
                //readCPUUsage();
                long totalMem, availMem;
                totalMem = mi.totalMem/1048576;
                availMem = mi.availMem/1048576;
                Bundle bundle = new Bundle();
                bundle.putInt(TAG_TOTAL_MEM, (int) totalMem);
                bundle.putInt(TAG_USAGE_MEM, (int) (totalMem-availMem) );
                bundle.putString(TAG_UPTIME, getUptime());
                Message msg = new Message();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }

        String getUptime() {
            long uptime = SystemClock.elapsedRealtime()/1000;
            int hour = (int)uptime/3600;
            int days = 0;
            if (hour > 23) {
                days = hour/24;
                hour = hour%24;
            }
            int minutes = (int) (uptime - hour*3600 - days*86400)/60;
            int sec = (int) (uptime - hour*3600 - days*86400 - minutes*60);
            StringBuilder sb = new StringBuilder("Uptime %d day(s) ");
            if (hour < 10)
                sb.append("0");
            sb.append("%d:");
            if (minutes < 10)
                sb.append("0");
            sb.append("%d:");
            if (sec < 10)
                sb.append("0");
            sb.append("%d");
            return String.format(sb.toString(), days, hour, minutes, sec);
        }

        private String readCPUUsage() {
            ProcessBuilder pb = new ProcessBuilder("top", "-n", "1", "-d", "2");
            //ProcessBuilder pb = new ProcessBuilder("cat", "/proc/cpuinfo");
            try {
                Process p = pb.start();
                p.waitFor();
                InputStream in = p.getInputStream();
                byte[] buffer = new byte[128];
                in.read(buffer, 0, 128);
                in.close();
                return new String(buffer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private int cpuUsageTotal(String str) {
            String[] strs = str.split("%");
            int cpu1, cpu2, cpu3, cpu4;
            cpu1 = getLastInt(strs[0]);
            cpu2 = getLastInt(strs[1]);
            cpu3 = getLastInt(strs[2]);
            cpu4 = getLastInt(strs[3]);

            return cpu1+cpu2+cpu3+cpu4;
        }

        private int getLastInt(String str) {
            return Integer.parseInt(str.substring(str.lastIndexOf(" ")+1));
        }

        private String getKbMbGb(float memory, float total) {
            String mem;
            if (memory < 1024) {
                mem = String.format("%.2f Mb / %.2f Mb", memory, total);
            } else {
                mem = String.format("%.2f Gb / %.2f Gb", memory/1024F, total/1024F);
            }
            return mem;
        }

        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {
                totalCPU.setText(String.format("%d %%", percentCpuUsage));
                totalCPUProgressBar.setProgress(percentCpuUsage);
                Bundle bundle = inputMessage.getData();
                int usageMem = bundle.getInt(TAG_USAGE_MEM);
                int totalMem = bundle.getInt(TAG_TOTAL_MEM);
                totalRAM.setText(getKbMbGb(usageMem, totalMem));
                long percent = usageMem*100/totalMem;
                int percent2 = (int) percent;
                totalRAMProgressBar.setProgress(percent2);
                uptime.setText(bundle.getString(TAG_UPTIME));
            }
        };
    }
}
