package com.bixin.launcher_t20.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bixin.launcher_t20.R;
import com.bixin.launcher_t20.model.bean.AppInfo;
import com.bixin.launcher_t20.model.listener.OnRecyclerViewItemListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RecyclerGridViewAdapter extends RecyclerView.Adapter<RecyclerGridViewAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<AppInfo> mData;
    private LayoutInflater inflater;
    private OnRecyclerViewItemListener mListener;

    public void setOnRecyclerViewItemListener(OnRecyclerViewItemListener listener) {
        this.mListener = listener;
    }

    public void setAppInfoArrayList(ArrayList<AppInfo> infoArrayList) {
        this.mData = infoArrayList;
    }

    public RecyclerGridViewAdapter(Context context, ArrayList<AppInfo> data) {
        this.mContext = context;
        this.mData = data;
        inflater = LayoutInflater.from(mContext);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_recycle_gridview, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder == null) {
            return;
        }
        AppInfo info = mData.get(position);
        holder.tvAppName.setText(info.getAppName());
        Glide.with(mContext).load(info.getAppIcon()).into(holder.ivAppIcon);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private WeakReference<RecyclerGridViewAdapter> adapterWeakReference;
        TextView tvAppName;
        ImageView ivAppIcon;

        private ViewHolder(final View itemView, RecyclerGridViewAdapter adapter) {
            super(itemView);
            adapterWeakReference = new WeakReference<>(adapter);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            ivAppIcon = itemView.findViewById(R.id.iv_app_icon);
            final RecyclerGridViewAdapter gridViewAdapter = adapterWeakReference.get();
            if (gridViewAdapter.mListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        String packageName = adapter.mData.get(position).getPkgName();
                        gridViewAdapter.mListener.onItemClickListener(position, packageName);
                    }
                });
//                itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        int position = getAdapterPosition();
//                        String packageName = adapter.mData.get(position).getPkgName();
//                        gridViewAdapter.mListener.onItemLongClickListener(position, packageName);
//                        return true;
//                    }
//                });
            }
        }
    }

}
