package cti.com.androidmonitor.fragments;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cti.com.androidmonitor.R;
import cti.com.androidmonitor.adapters.ProcessAdapter;
public class Processes extends Fragment {

    RecyclerView recyclerView;

    ProcessAdapter adapter;

    ProgressBar loadingProgressBar;

    ProcessBuilder processBuilder;

    private UpdateRecyclerView updateRecyclerView;

    private final String TAG_STATE_ADAPTER = "STATE_ADAPTER";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.processes_fragment, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.universal_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        loadingProgressBar = (ProgressBar) view.findViewById(R.id.progressBar_loading);
        if (savedInstanceState != null) {
            String array[] = savedInstanceState.getStringArray(TAG_STATE_ADAPTER);
            if (array != null) {
                createAdapter(array);
                loadingProgressBar.setVisibility(View.INVISIBLE);
                recyclerView.setAdapter(adapter);
            }
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateRecyclerView = new UpdateRecyclerView();
        updateRecyclerView.start();
    }

    private class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int pos = recyclerView.getChildLayoutPosition(v);
            if (pos != 0) {
                new ViewDialog().showDialog(adapter.getItem(pos), pos);
            }
        }
    }

    private class UpdateRecyclerView implements Runnable {

        Thread t;
        volatile boolean flagProcess = true;

        public void start() {
            t = new Thread(this);
            t.start();
        }

        public void stop() {
            flagProcess = false;
        }

        @Override
        public void run() {
            if (adapter == null) {
                createAdapter(getArray(getProcesses()));
            }
            loadingProgressBar.post(new Runnable() {
                @Override
                public void run() {
                    loadingProgressBar.setVisibility(View.INVISIBLE);
                }
            });
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setAdapter(adapter);
                }
            });
            while (flagProcess) {
                adapter.setData(getArray(getProcesses()));
                //adapter.notifyDataSetChanged();
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    private void createAdapter(String array[]) {
        adapter = new ProcessAdapter(array, getActivity(), new MyOnClickListener());
    }

    private String getProcesses() {
        try {
            //ProcessBuilder pb = new ProcessBuilder("cat", "/proc/stat");
            processBuilder = new ProcessBuilder("top", "-n", "1", "-d", "2");
            //ProcessBuilder pb = new ProcessBuilder("cat", "/proc/cpuinfo");
            Process p = processBuilder.start();
            p.waitFor();
            InputStream in = p.getInputStream();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer, 0, 1024)) > 0) {
                buf.write(buffer, 0, read);
            }
            in.close();
            String str = new String(buf.toByteArray()).trim();
            buf.close();
            return str;
            //FileWriter fw = new FileWriter("/mnt/result.txt");
            //fw.write(str);
            //fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPIDFromTop(String str) {
        int position = 0;
        for (;position < str.length(); position++) {
            if (str.charAt(position) != ' ') break;
        }
        if (position != 0) {
            str = str.substring(position);
        }
        return str.substring(0, str.indexOf(" "));
    }

    private String[] getArray(String str) {
        return str.split("\\r?\\n");
    }

    private class ViewDialog implements Runnable {

        private TextView memProc, cpuProc;

        volatile float PSS;

        private volatile boolean flagShowDialog = true;

        String pid;

        private int positionProc;

        public void showDialog(String name, int pos) {
            Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_process);
            positionProc = pos;
            memProc = (TextView) dialog.findViewById(R.id.processMem_dialog);
            cpuProc = (TextView) dialog.findViewById(R.id.processCPU_dialog);
            ((TextView) dialog.findViewById(R.id.processName_dialog)).setText(name.substring(name.lastIndexOf(" ")));
            pid = getPIDFromTop(name);
            ((TextView) dialog.findViewById(R.id.processPID_dialog)).setText("PID "+pid);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    flagShowDialog = false;
                }
            });
            Thread t = new Thread(this);
            t.start();
            dialog.show();
        }

        @Override
        public void run() {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            while (flagShowDialog) {
                Debug.MemoryInfo mem[] = activityManager.getProcessMemoryInfo(new int[]{Integer.parseInt(pid)});
                PSS = (float) mem[0].getTotalPss()/1024;
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {
                memProc.setText(String.format("Memory %.2f MB", PSS));
                cpuProc.setText("CPU% "+adapter.getCPU(positionProc));

            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        updateRecyclerView.stop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null) {
            String array[] = adapter.getData();
            if (array != null)
                outState.putStringArray(TAG_STATE_ADAPTER, array);
        }
    }
}
