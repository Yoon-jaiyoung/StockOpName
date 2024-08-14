package com.parkland.stockopname;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapter_MAT extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    public ArrayList<String> mItemList;
    Context mContext;
    ItemClickListener listener;


    public RecyclerViewAdapter_MAT(Context mContext, ArrayList<String> itemList, ItemClickListener listener) {
        mItemList = itemList;
        this.mContext = mContext;
        this.listener = listener;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View mView  = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_mat, parent, false);
            final ViewHolder mViewHolder = new ViewHolder(mView);
            mView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    listener.recyclerViewListClicked(v, mViewHolder.getAdapterPosition());
                }
            });

            return mViewHolder;
        } else {
            View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(mView);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof ViewHolder) {
            populateItemRows((ViewHolder) viewHolder, position);
        } else if (viewHolder instanceof LoadingViewHolder) {
            showLoadingView((LoadingViewHolder) viewHolder, position);
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNo, tvBarcode, tvTime, tvAct, tvOX;
        ViewHolder(View v) {
            super(v);
            tvNo = itemView.findViewById(R.id.tvNo);
            tvBarcode = itemView.findViewById(R.id.tvBarcode);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvAct = itemView.findViewById(R.id.tvAct);
            tvOX = itemView.findViewById(R.id.tvOX);
        }
    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    /**
     * The following method decides the type of ViewHolder to display in the RecyclerView
     *
     * @param position
     * @return
     */

    @Override
    public int getItemViewType(int position) {

        return mItemList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }


    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed

    }

    private void populateItemRows(ViewHolder viewHolder, int position) {
        if(position < mItemList.size()) {
            String item = mItemList.get(position);
            viewHolder.tvNo.setText(item.split(",")[0]);
            viewHolder.tvBarcode.setText(item.split(",")[1]);
            viewHolder.tvTime.setText(item.split(",")[2]);
            viewHolder.tvAct.setText(item.split(",")[3]);
            viewHolder.tvOX.setText(item.split(",")[4]);

        }
    }
}