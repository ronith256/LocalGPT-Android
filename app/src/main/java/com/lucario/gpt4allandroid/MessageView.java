package com.lucario.gpt4allandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import android.print.PrintAttributes;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MessageView extends AppCompatActivity implements MessageAdapter.MessageDoneListener{
    RecyclerView recyclerView;
    EditText messageEditText;
    ImageButton sendButton;
    static List<Message> messageList;
    JSONArray messageArray;
    static MessageAdapter messageAdapter;

    TextView timerText;

    public static int sessionTimeout, httpSessionTimeout;
    private File chatFile;
    private Chat chat;
    private List<Chat> mChatList;
    private String question;
    long prevTime;
    private static final int PERMISSION_REQUEST_CODE = 111;
    private ActivityResultLauncher<Intent> launcher;

    private long model;
    private int ctxSize;

    private boolean isChat = true;
    private StringBuilder builder;

    private String response = "";

    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = getSharedPreferences("nums", MODE_PRIVATE);
        ctxSize = pref.getInt("ctx-size", 2048);
        model = getIntent().getLongExtra("model-pointer", 0);
        if(model == 0){
            finish();
        }
        setContentView(R.layout.message_view);

        ImageButton settingsButton = findViewById(R.id.toolbar_settings);
        settingsButton.setOnClickListener(e -> {
            SettingsDialogFragment dialog = new SettingsDialogFragment();
            dialog.show(getSupportFragmentManager(), "SettingsDialogFragment");
        });

        if(isChat){
            builder = new StringBuilder();
        }

        mChatList = (List<Chat>) getIntent().getSerializableExtra("chatList");
        int s = getIntent().getIntExtra("chat", 0);
        System.out.println(s);
        chat = mChatList.get(s);
        chatFile = chat.getChatArray();
        messageList = new ArrayList<>();
        messageArray = new JSONArray();

        loadChatList(chatFile);

        CardView buttonsView = findViewById(R.id.buttonsView);
        AppCompatButton chatButton = findViewById(R.id.chatButton);
        AppCompatButton completionButton = findViewById(R.id.textButton);
        chatButton.setOnClickListener(e->{buttonsView.setVisibility(View.GONE);});
        completionButton.setOnClickListener(e->{isChat = false; buttonsView.setVisibility(View.GONE);});


        recyclerView = findViewById(R.id.recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        //setup recycler view
        messageAdapter = new MessageAdapter(messageList, this, this);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.getItemAnimator().setChangeDuration(0);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendButton.setOnClickListener((v) -> {
            buttonsView.setVisibility(View.GONE);
            MainActivity.createEmptyTextFile();
            String question = messageEditText.getText().toString().trim();
            addToChat(question, Message.SENT_BY_ME);
            messageEditText.setText("");
            callAPI(question);
        });
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy));
            saveChatList(chatFile.getName(), messageList);
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    private void saveChatList(String name, List saveList) {
        // Write the chat list to a file
        try {
            FileOutputStream fos = openFileOutput(name, MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(saveList);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChatList(File name) {
        // Read the chat list from a file
        try {
            FileInputStream fis = openFileInput(name.getName());
            ObjectInputStream ois = new ObjectInputStream(fis);
            messageList = (ArrayList<Message>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
//            Toast.makeText(this, "Load failed", Toast.LENGTH_SHORT).show();
        }
        // Initialize the chat list if it doesn't exist
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
    }


    // Handle the permission request response
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Microphone permission is not granted!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    void addResponse(String response, boolean failed) {
//        messageList.remove(messageList.size()-1);
        if (failed) {
            addToChat(response, Message.FAILED_RESPONSE);
        } else {
            addToChat(response, Message.SENT_BY_BOT);
        }

        mChatList.set(chat.getChatId() - 1, chat);
        saveChatList("chat_list.ser", mChatList);
    }


    void callAPI(String question) {
        if (chat.getChatName().equals("null") || !(chat.getChatName().length() > 1)) {
            if (question.length() > 15) {
                chat.setChatName(question.substring(0, 15));
            } else {
                chat.setChatName(question);
            }
            mChatList.set(chat.getChatId() - 1, chat);
            saveChatList("chat_list.ser", mChatList);
        }

      sendConvoMessage(question);
    }

    private void sendConvoMessage(String message) {
        addResponse("", false);
        ctxSize =  getSharedPreferences("nums", MODE_PRIVATE).getInt("ctx-size", 2048);
        if(isChat && !(builder.length() >1)){
            String chatStuff = "Human: [Usermessage]\nAI: [Assistant response]\n\nHuman: ";
            message = chatStuff + message + "\n\nAI:";
            builder.append(message);
        } else {
            builder.append(" ");
            builder.append(response);
            builder.append("\n\nHuman: ");
            builder.append(message);
            builder.append("\n\nAI:");
        }
        sendMessage(builder.toString());
    }


    private void sendMessage(String message) {
//       String sMessage = "### Human:" + message;
//       sMessage = sMessage + "\\n### Assistant:";
//        System.out.println(sMessage);
        String finalSMessage = message;
        new Thread(new Runnable() {
           @Override
           public void run() {
               MainActivity.prompt(model, finalSMessage, ctxSize);
           }
       }).start();
        new Thread(() -> watchFileForChanges()).start();
    }

    private void addMessage(String text, boolean done){
        if (done) {
//            messageList.get(messageList.size()-1).message = text;
//            runOnUiThread(() -> messageAdapter.notifyItemChanged(messageList.size() - 1));
            Message msg = messageList.get(messageList.size() - 1);
            msg.setFinished(true);
            if (msg.getMessage().length() > 20) {
                chat.setLatestChat(msg.getMessage().substring(0, 17) + "...");
            } else {
                chat.setLatestChat(msg.getMessage());
            }
            new Thread(() -> {
                saveChatList(chatFile.getName(), messageList);
                mChatList.set(chat.getChatId() - 1, chat);
                saveChatList("chat_list.ser", mChatList);
            }).start();
        } else {
            response = text;
            messageList.get(messageList.size() - 1).message = text;
            runOnUiThread(() -> messageAdapter.notifyItemChanged(messageList.size() - 1));
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(messageList.size() > 1){
            Message msg = messageList.get(messageList.size() - 1);
            msg.setFinished(true);
            if (msg.getMessage().length() > 20) {
                chat.setLatestChat(msg.getMessage().substring(0, 17) + "...");
            } else {
                chat.setLatestChat(msg.getMessage());
            }
        }
        new Thread(() -> {
            saveChatList(chatFile.getName(), messageList);
            mChatList.set(chat.getChatId() - 1, chat);
            saveChatList("chat_list.ser", mChatList);
        }).start();
        finish();
    }

    @Override
    public void setFirstTime(int position) {
        messageList.get(position).setFirstTime(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveChatList(chatFile.getName(), messageList);
            }
        }).start();
    }

    public void watchFileForChanges() {
        try {
            String builder = "";
            long time = System.currentTimeMillis();
            Path path = Paths.get("/storage/emulated/0/Android/data/com.lucario.gpt4allandroid/files/Documents/response.txt");
            WatchService watchService = FileSystems.getDefault().newWatchService();
            path.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException ex) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.context().toString().equals(path.getFileName().toString())) {
                        // Read content of modified file and print it
                        String content = new String(Files.readAllBytes(path));
                        if(content.length() > builder.length()) {
                            builder = content;
                            time = System.currentTimeMillis();
                            if (isChat && content.contains("Human:")) {
                                addMessage(content, true);
                                return;
//                                System.out.println("done");
                            }
                            addMessage(content, false);
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }

//                if(System.currentTimeMillis()-time > 1000){
//                    addMessage("", true);
//                    break;
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}