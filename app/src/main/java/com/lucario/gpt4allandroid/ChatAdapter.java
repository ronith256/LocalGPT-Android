package com.lucario.gpt4allandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context mContext;
    private List<Chat> mChatList;
    private onClick listener;
    private deleteItem deleteItemListener;
    public ChatAdapter(Context context, List<Chat> chatList, onClick listener, deleteItem deleteItemListener) {
        mContext = context;
        mChatList = chatList;
        this.listener = listener;
        this.deleteItemListener = deleteItemListener;
    }

    public void setChatList(List<Chat> mChatList){
        this.mChatList = mChatList;
    }
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_heads, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = mChatList.get(position);
        holder.profileName.setText(chat.getChatName());
        holder.latestChat.setText(chat.getLatestChat());
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView profileLogo;
        public TextView profileName;
        public TextView latestChat;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            profileLogo = itemView.findViewById(R.id.profile_logo);
            profileName = itemView.findViewById(R.id.profile_name);
            latestChat = itemView.findViewById(R.id.latest_chat);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.clicked(getAdapterPosition());
        }
    }
    public interface onClick{
        void clicked(int position);
    }

    public Context getContext(){
        return mContext;
    }

    public void deleteItem(int position){
        deleteItemListener.deleteItem(position);
    }
    public interface deleteItem{
        public void deleteItem(int position);
    }
}
