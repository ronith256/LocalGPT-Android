package com.lucario.gpt4allandroid;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    List<Message> messageList;
    MessageDoneListener listener;
    Context context;

    ClipboardManager clipboardManager;
    public MessageAdapter(List<Message> messageList, MessageDoneListener listener, Context context) {
        this.messageList = messageList;
        this.listener = listener;
        this.context = context;
        this.clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,null);
        MyViewHolder myViewHolder = new MyViewHolder(chatView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.rightTextView.setOnLongClickListener(e->{
            copyToClipboard(holder.rightTextView.getText().toString());
            Toast.makeText(context, "Copied Text", Toast.LENGTH_SHORT).show();
            return true;
        });

        holder.leftTextView.setOnLongClickListener(e->{
            copyToClipboard(holder.leftTextView.getText().toString());
            Toast.makeText(context, "Copied Text", Toast.LENGTH_SHORT).show();
            return true;
        });

        if (message.getSentBy().equals(Message.SENT_BY_ME)) {
            holder.leftChatView.setVisibility(View.GONE);
            holder.rightChatView.setVisibility(View.VISIBLE);
            holder.rightTextView.setText(message.getMessage());
        } else {
            holder.rightChatView.setVisibility(View.GONE);
            holder.leftChatView.setVisibility(View.VISIBLE);
            if (message.finished) {
                holder.leftTextView.setText(message.getMessage());
            } else {
                holder.leftTextView.setText(message.getMessage());
            }
        }
    }

    private void copyToClipboard(String text) {
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboardManager.setPrimaryClip(clip);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        LinearLayout leftChatView,rightChatView;
        TextView leftTextView,rightTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatView  = itemView.findViewById(R.id.left_chat_view);
            rightChatView = itemView.findViewById(R.id.right_chat_view);
            leftTextView = itemView.findViewById(R.id.left_chat_text_view);
            rightTextView = itemView.findViewById(R.id.right_chat_text_view);
        }
    }

    public interface MessageDoneListener{
        public void setFirstTime(int position);
    }
}

