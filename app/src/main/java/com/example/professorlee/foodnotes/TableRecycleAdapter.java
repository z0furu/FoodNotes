package com.example.professorlee.foodnotes;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TableRecycleAdapter extends RecyclerView.Adapter<TableRecycleAdapter.ViewHolder> implements View.OnClickListener {

    private static final String TAG = "TableRecycleAdapter";

    private List<TableItem> tableItems;
    private Context mContext;

    private ItemClickListener itemClickListener = null;

    public interface ItemClickListener {
        void onItemClick(View view);
    }

    public TableRecycleAdapter(List<TableItem> tableItems, Context mContext) {
        this.tableItems = tableItems;
        this.mContext = mContext;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView shopname;
        private TextView location;
        private ImageView imageView;
        private CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            cardView.setCardBackgroundColor(Color.argb(255,225,245,254));
            shopname = (TextView) itemView.findViewById(R.id.name);
            location = (TextView) itemView.findViewById(R.id.location);
            imageView = (ImageView) itemView.findViewById(R.id.image);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.shopname.setText(tableItems.get(position).getShopname());
        holder.location.setText(tableItems.get(position).getLocation());

        Glide.with(mContext).load("http://skychi.no-ip.org/Lu/food_note/upload/" + tableItems.get(position).getFoodimage())
                .centerCrop()
                .into(holder.imageView);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.table_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(this);

        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return tableItems.size();
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener != null) {
            itemClickListener.onItemClick(v);
        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
