package cti.com.androidmonitor.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cti.com.androidmonitor.R;

public class ProcessAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String[] strs;

    private LayoutInflater inflater;

    View.OnClickListener clickListener;


    public ProcessAdapter(String[] strs, Context context, View.OnClickListener clickListener) {
        setData(strs);
        this.clickListener = clickListener;
        if (context != null) {
            inflater = LayoutInflater.from(context);
        }
    }

    public void setData(String[] strs) {
        this.strs = new String[strs.length-3];
        for (int i = 3; i < strs.length; i++) {
            this.strs[i-3] = new String(strs[i]);
        }
        /*char ch = '|';
        Log.d("TAG", strs.length+" "+(int)ch);
        Runtime r = Runtime.getRuntime();
        ProcessBuilder pb = new ProcessBuilder("ps", String.valueOf((char)124),"wc", "-l");
        try {
            Process p = pb.start();
            p.waitFor();
            InputStream fin = p.getInputStream();
            byte buff[] = new byte[1024];
            int count = fin.read(buff, 0, 1024);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bout.write(buff, 0, count);
            Log.d("TAG", new String(bout.toByteArray())+" second length");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.processes_item, parent, false);
        view.setOnClickListener(clickListener);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String str2 = strs[position].trim();
        ((MyViewHolder) holder).id.setText(str2.substring(0, str2.indexOf(" ")));
        ((MyViewHolder) holder).cpu.setText(getCPU(position));
        ((MyViewHolder) holder).name.setText(str2.substring(str2.lastIndexOf(" ")+1));
    }

    @Override
    public int getItemCount() {
        return strs.length;
    }

    public String[] getData() {
        return strs.clone();
    }

    public String getItem(int position) {
        return strs[position];
    }

    public String getCPU(int position) {
        String str = strs[position].substring(0, strs[position].indexOf("%")+1);
        return str.substring(str.lastIndexOf(" ")+1);
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        TextView id, name, cpu;

        public MyViewHolder(View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.processId_textView);
            cpu = (TextView) itemView.findViewById(R.id.processCPU_textView);
            name = (TextView) itemView.findViewById(R.id.processName_textView);
        }
    }
}
