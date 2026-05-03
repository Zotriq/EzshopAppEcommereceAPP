package com.example.ezshopapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onWriteReviewClick(Order order);
    }

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    public OrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        if (order.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            holder.tvOrderDate.setText("Date: " + sdf.format(order.getTimestamp()));
        } else {
            holder.tvOrderDate.setText("Date: N/A");
        }

        holder.tvOrderStatus.setText(order.getStatus());
        
        StringBuilder itemsBuilder = new StringBuilder();
        if (order.getItems() != null) {
            for (int i = 0; i < order.getItems().size(); i++) {
                CartItem item = order.getItems().get(i);
                itemsBuilder.append(item.getProductName()).append(" x").append(item.getQuantity());
                if (i < order.getItems().size() - 1) {
                    itemsBuilder.append(", ");
                }
            }
        }
        holder.tvOrderItems.setText(itemsBuilder.toString());
        holder.tvOrderTotal.setText(String.format(Locale.getDefault(), "$ %.2f", order.getTotalAmount()));

        // Show "Write Review" button ONLY if status is Completed
        if ("Completed".equalsIgnoreCase(order.getStatus())) {
            holder.btnWriteReview.setVisibility(View.VISIBLE);
        } else {
            holder.btnWriteReview.setVisibility(View.GONE);
        }

        holder.btnWriteReview.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWriteReviewClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderDate, tvOrderStatus, tvOrderItems, tvOrderTotal;
        Button btnWriteReview;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            btnWriteReview = itemView.findViewById(R.id.btnWriteReview);
        }
    }
}
