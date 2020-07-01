package cti.com.androidmonitor.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cti.com.androidmonitor.R;

public class PingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> data;

    private LayoutInflater inflater;

    private int JUST_ITEM = 0, LAST_ITEM = 1;

    private volatile int VISIBLE_PROGRESS_BAR = View.VISIBLE;

    public PingAdapter(Context context) {
        data = new ArrayList<String>();
        if (context != null) {
            inflater = LayoutInflater.from(context);
        }
    }

    public void addData(String item) {
        data.add(item);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == JUST_ITEM) {
            view = inflater.inflate(R.layout.ping_item, parent, false);
            return new MyViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.item_progress_bar, parent, false);
            return new MyViewHolderLast(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).result.setText(data.get(position));
        } else {
            ((MyViewHolderLast) holder).progressBar.setVisibility(VISIBLE_PROGRESS_BAR);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionBottom(position))
            return LAST_ITEM;
        return JUST_ITEM;
    }

    private boolean isPositionBottom(int position) {
        return position == data.size();
    }

    public void setVISIBLE_PROGRESS_BAR(int VIS) {
        VISIBLE_PROGRESS_BAR = VIS;
    }

    @Override
    public int getItemCount() {
        return data.size() + 1;
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        TextView result;

        public MyViewHolder(View itemView) {
            super(itemView);
            result = (TextView) itemView.findViewById(R.id.pingResult_textView);
        }
    }

    private class MyViewHolderLast extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public MyViewHolderLast(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar_loading);
        }
    }
}
