package com.example.chatapp.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.ItemContainerReceivedMessBinding;
import com.example.chatapp.databinding.ItemContainerSentMessBinding;
import com.example.chatapp.models.ChatMess;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMess> chatMesses;
    private  Bitmap receiverProfileImg;
    private final  String senderId;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT) {
            return new SentMessViewHolder(
                    ItemContainerSentMessBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else {
            return new ReceivedMessViewHolder(
                    ItemContainerReceivedMessBinding.inflate(LayoutInflater.from(parent.getContext()), parent,false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessViewHolder) holder).setData(chatMesses.get(position));
        } else {
            ((ReceivedMessViewHolder) holder).setData(chatMesses.get(position), receiverProfileImg);
        }
    }

    @Override
    public int getItemCount() {
        return chatMesses.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMesses.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }
    public void setReceiverProfileImg(Bitmap bitmap) {
        receiverProfileImg = bitmap;
    }
    public ChatAdapter(List<ChatMess> chatMesses, Bitmap receiverProfileImg, String senderId) {
        this.chatMesses = chatMesses;
        this.receiverProfileImg = receiverProfileImg;
        this.senderId = senderId;
    }

    static class SentMessViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerSentMessBinding binding;

        SentMessViewHolder(ItemContainerSentMessBinding itemContainerSentMessBinding){
            super(itemContainerSentMessBinding.getRoot());
            binding = itemContainerSentMessBinding;
        }

        void setData(ChatMess chatMess) {
            binding.textMess.setText(chatMess.message);
            binding.textDateTime.setText(chatMess.dateTime);
        }
    }

    static class ReceivedMessViewHolder extends RecyclerView.ViewHolder {

        private  final ItemContainerReceivedMessBinding binding;

        ReceivedMessViewHolder(ItemContainerReceivedMessBinding itemContainerReceivedMessBinding) {
            super(itemContainerReceivedMessBinding.getRoot());
            binding = itemContainerReceivedMessBinding;
        }

        void setData(ChatMess chatMess, Bitmap receiverProfileImg) {
            binding.textMess.setText(chatMess.message);
            binding.txtDateTime.setText(chatMess.dateTime);
            if(receiverProfileImg != null) {
                binding.imgProfile.setImageBitmap(receiverProfileImg);
            }
        }
    }
}
