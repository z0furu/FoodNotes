package com.lu.luandroid.foodnotes;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class TableItem_searchRecycleAdapter extends RecyclerView.Adapter<TableItem_searchRecycleAdapter.ViewHolder> implements View.OnClickListener {

    private List<TableItem_search_food> tableItem_search_foods;
    private Context mContext;


    private ItemClickListener itemClickListener = null;

    public interface ItemClickListener {
        void onItemClick(View view);
    }

    public TableItem_searchRecycleAdapter(List<TableItem_search_food> tableItems, Context mContext) {
        this.tableItem_search_foods = tableItems;
        this.mContext = mContext;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView location;
        private TextView distance;

        public ViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            location = (TextView) itemView.findViewById(R.id.location);

            distance = (TextView) itemView.findViewById(R.id.distance);
        }
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(tableItem_search_foods.get(position).getName());
        holder.location.setText(tableItem_search_foods.get(position).getLocation());

        holder.distance.setText(tableItem_search_foods.get(position).getDistance());

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.table_item_search_food, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(this);

        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return tableItem_search_foods.size();
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
