package cti.com.androidmonitor.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cti.com.androidmonitor.R;
import cti.com.androidmonitor.adapters.PingAdapter;
import cti.com.androidmonitor.parser.PingParser;

public class Ping extends Fragment {

    private EditText address, counter, bytes, interval;

    private PingThread pingThread;

    private RecyclerView pingResult;

    private PingAdapter pingAdapter;

    Button startPingBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ping_fragment, container, false);
        address = (EditText) view.findViewById(R.id.address_editText);
        counter = (EditText) view.findViewById(R.id.count_EditText);
        bytes = (EditText) view.findViewById(R.id.bytes_EditText);
        interval = (EditText) view.findViewById(R.id.interval_EditText);
        pingThread = new PingThread();
        pingResult = (RecyclerView) view.findViewById(R.id.ping_recyclerView);
        pingResult.setLayoutManager(new LinearLayoutManager(getActivity()));
        startPingBtn = (Button) view.findViewById(R.id.ping_button);
        view.findViewById(R.id.ping_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pingThread.getFlag()) {
                    pingThread.stop();
                    setStartButton();
                } else {
                    pingThread.start();
                    startPingBtn.setText("Stop");
                    startPingBtn.setTextColor(getResources().getColor(R.color.colorPrimaryHightDark));
                }
            }
        });
        return view;
    }

    private void setStartButton() {
        startPingBtn.setText("Start");
        startPingBtn.setTextColor(Color.WHITE);
    }

    private class PingThread implements Runnable {

        private volatile boolean flagExecute;

        private Thread t;

        void start() {
            flagExecute = true;
            t = new Thread(this);
            t.start();
        }

        private void stop() {
            flagExecute = false;
        }

        public boolean getFlag() {
            synchronized (this) {
                return flagExecute;
            }
        }

        @Override
        public void run() {

            String size = bytes.getText().toString();
            String urlAddress = address.getText().toString();
            int count = Integer.parseInt(counter.getText().toString());
            pingAdapter = new PingAdapter(getActivity());
            //pingResult.setAdapter(pingAdapter);
            pingResult.post(new Runnable() {
                @Override
                public void run() {
                    pingResult.setAdapter(pingAdapter);
                }
            });
            long intervalTime = Long.parseLong(interval.getText().toString())*1000L;
            for (int i = 0; i < count && flagExecute; i++) {
                ProcessBuilder pb = new ProcessBuilder("ping", "-c", "1", "-s", size, urlAddress);
                try {
                    Process process = pb.start();
                    process.waitFor();
                    InputStream in = process.getInputStream();
                    ByteArrayOutputStream buf = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer, 0, 1024)) > 0) {
                        buf.write(buffer, 0, read);
                    }
                    String result = new String(buf.toByteArray());
                    buf.close();
                    in.close();
                    pingAdapter.addData(PingParser.getResult(result));
                    updateItem(pingAdapter.getItemCount()-1);
                    Thread.sleep(intervalTime);
                } catch (IOException e) {
                    Log.e("Prosess problem.", "Problem to execute cmd ping.");
                } catch (InterruptedException e) {
                    Log.e("Ping. Prosess problem.", "Problem to execute cmd ping and waitFor.");
                }
            }
            pingAdapter.setVISIBLE_PROGRESS_BAR(View.INVISIBLE);
            updateItem(pingAdapter.getItemCount()-1);
            stop();
            startPingBtn.post(new Runnable() {
                @Override
                public void run() {
                    setStartButton();
                }
            });
        }

        private void updateItem(final int position) {
            pingResult.post(new Runnable() {
                @Override
                public void run() {
                    pingAdapter.notifyItemChanged(position);
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pingThread.stop();
    }
}
