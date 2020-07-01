package cti.com.androidmonitor.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cti.com.androidmonitor.R;
import cti.com.androidmonitor.linpack.CustomLinpack;
import cti.com.androidmonitor.linpack.adapters.AdapterLinpack;
import cti.com.androidmonitor.widget.CustomProgressBar;

public class LinpackTest extends Fragment implements View.OnClickListener {

    private final String TAG_TIME = "TIME";
    private final String TAG_SIZE = "SIZE";

    private Context context;

    CustomProgressBar progressBar;

    LinpackInBackground lib;

    private boolean flagExecute = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.linpack_fragment, container, false);
        view.findViewById(R.id.startTest_button).setOnClickListener(this);
        progressBar = (CustomProgressBar) view.findViewById(R.id.customProgressBar);

        context = getActivity();

        lib = new LinpackInBackground();
        return view;
    }

    private void saveStatus(String time, String size) {
        SharedPreferences shPre = context.getSharedPreferences("RESULT", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor ed = shPre.edit();
        ed.putString(TAG_TIME, time);
        ed.putString(TAG_SIZE, size);
        ed.commit();
    }

    private void loadStatus() {
        SharedPreferences shPre = context.getSharedPreferences("RESULT", getActivity().MODE_PRIVATE);
        progressBar.setBigText(shPre.getString("TIME", ""));
    }

    @Override
    public void onStart() {
        super.onStart();
        loadStatus();
    }

    @Override
    public void onClick(View v) {
        if (!flagExecute) {
            flagExecute = true;
            lib.executeOne();
        }
    }

    private class LinpackInBackground implements Runnable, AdapterLinpack {

        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {
                progressBar.setBigText(inputMessage.getData().getString(TAG_TIME) );
            }
        };

        public void executeOne() {
            Thread t = new Thread(this);
            t.start();
        }

        @Override
        public void run() {
            int size = 1000;
            CustomLinpack cl = new CustomLinpack(size, this);
            cl.generateMatrix();
            String flops = cl.getFlops();
            Bundle bundle = new Bundle();
            bundle.putString(TAG_TIME, flops);
            saveStatus(flops, String.valueOf(size));
            Message msg = new Message();
            msg.setData(bundle);
            handler.sendMessage(msg);
            flagExecute = false;
        }

        @Override
        public void setPercent(final int percent) {
            progressBar.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(percent);
                    progressBar.postInvalidate();
                }
            });
        }
    }
}
