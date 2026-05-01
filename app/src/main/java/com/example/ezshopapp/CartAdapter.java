package com.example.ezshopapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItems;
    private final OnCartActionClickListener listener;
    private boolean isCheckoutMode = false;

    public interface OnCartActionClickListener {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
        void onDelete(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartActionClickListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public CartAdapter(List<CartItem> cartItems, OnCartActionClickListener listener, boolean isCheckoutMode) {
        this.cartItems = cartItems;
        this.listener = listener;
        this.isCheckoutMode = isCheckoutMode;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.name.setText(item.getProductName());
        holder.price.setText("$ " + (item.getPrice() * item.getQuantity()));
        holder.color.setText("Color: " + item.getSelectedColor());
        holder.quantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.bg_product_image_grey)
                .into(holder.image);

        if (isCheckoutMode) {
            holder.btnPlus.setVisibility(View.GONE);
            holder.btnMinus.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnPlus.setVisibility(View.VISIBLE);
            holder.btnMinus.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            
            holder.btnPlus.setOnClickListener(v -> { if (listener != null) listener.onIncrease(item); });
            holder.btnMinus.setOnClickListener(v -> { if (listener != null) listener.onDecrease(item); });
            holder.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(item); });
        }
    }

    @Override
    public int getItemCount() { return cartItems.size(); }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView image, btnDelete, btnPlus, btnMinus;
        TextView name, price, color, quantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cartProductImage);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            name = itemView.findViewById(R.id.cartProductName);
            price = itemView.findViewById(R.id.cartProductPrice);
            color = itemView.findViewById(R.id.cartProductColor);
            quantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}
