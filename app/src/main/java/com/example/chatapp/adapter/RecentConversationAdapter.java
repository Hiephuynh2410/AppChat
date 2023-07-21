package com.example.chatapp.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.ItemContainerRecentBinding;
import com.example.chatapp.listeners.ConversionListener;
import com.example.chatapp.models.ChatMess;
import com.example.chatapp.models.User;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversationViewHolder> {

    private final List<ChatMess> chatMesses;
    private final ConversionListener conversionListenser;

    public RecentConversationAdapter(List<ChatMess> chatMesses, ConversionListener conversionListener) {
        this.chatMesses = chatMesses;
        this.conversionListenser = conversionListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
                ItemContainerRecentBinding.inflate(
                      LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(chatMesses.get(position ));
    }

    @Override
    public int getItemCount() {
        return chatMesses.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentBinding binding;
        ConversationViewHolder(ItemContainerRecentBinding itemContainerRecentBinding) {
            super(itemContainerRecentBinding.getRoot());
            binding = itemContainerRecentBinding;
        }

        void setData(ChatMess chatMess) {
            binding.imageProfile.setImageBitmap(getConversationImg(chatMess.conversionImage));
            binding.textName.setText(chatMess.conversionName);
            binding.textRecentMess.setText(chatMess.message );
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMess.conversionId;
                user.name = chatMess.conversionName;
                user.image = chatMess.conversionImage;
                conversionListenser.onConversionClicked(user);
            });
        }
    }

    private Bitmap getConversationImg(String encodeImg){
        byte[] bytes = Base64.decode(encodeImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
