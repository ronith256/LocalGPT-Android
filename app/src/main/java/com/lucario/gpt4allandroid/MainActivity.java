package com.lucario.gpt4allandroid;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JavaJniMissingFunction")
public class MainActivity extends AppCompatActivity implements ChatAdapter.onClick, ChatAdapter.deleteItem {

    private static String modelPath = "/storage/emulated/0/Android/data/com.lucario.gpt4allandroid/files/Documents/ggml-gpt4all-j-v1.3-groovy.bin";
    public static native void prompt(long model, String prompt, int contextSize);

    public static native void destoryModel(long model);
    public static native long loadGo(String fname, int nThreads);
    private ChatAdapter mAdapter;
    private List<Chat> mChatList;

    private Intent messageViewIntent;

    private int numThreads = 0;
    private FloatingActionButton newChatButton;
    static SharedPreferences pref;
    private static long model;
    private boolean modelExists = false;

    private long downloadID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("nums", MODE_PRIVATE);
        modelExists = pref.getBoolean("model", false);
        numThreads = pref.getInt("num-threads", 4);
        RecyclerView mRecyclerView = findViewById(R.id.chat_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ImageButton settingsButton = findViewById(R.id.toolbar_settings);
        settingsButton.setOnClickListener(e -> {
            SettingsDialogFragment dialog = new SettingsDialogFragment();
            dialog.show(getSupportFragmentManager(), "SettingsDialogFragment");

        });

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (id == downloadID) {
                        File sourceFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ggml-gpt4all-j-v1.3-groovy.bin");
                        File destinationFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ggml-gpt4all-j-v1.3-groovy.bin");
                        boolean isMoved = sourceFile.renameTo(destinationFile);
                        Ddialog.setCancelable(true);
                        Ddialog.dismiss();
                        showLoading();
                        new Thread(() -> {
                            createEmptyTextFile();
                            model = loadGo(modelPath, numThreads);
                        }).start();
                    }
                }
            }
        };

        File modelFile = new File(modelPath);
        if(!modelFile.exists()){
            String downloadUrl = "https://gpt4all.io/models/ggml-gpt4all-j-v1.3-groovy.bin";
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setTitle("File Download");
            request.setDescription("Downloading file...");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, "ggml-gpt4all-j-v1.3-groovy.bin");
            downloadID = downloadManager.enqueue(request);
            registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            showDownload();
        } else {
            pref.edit().putBoolean("model", true).apply();
            showLoading();
            new Thread(() -> {
                createEmptyTextFile();
                model = loadGo(modelPath, numThreads);
            }).start();
        }

        newChatButton = findViewById(R.id.fab_new_chat);
        mChatList = new ArrayList<>();
        loadChatList();


        newChatButton.setOnClickListener(e->{
            messageViewIntent = new Intent(MainActivity.this, MessageView.class);
            Chat chat = new Chat(mChatList.size()+1, 0, "null", "null", new File(String.valueOf(System.currentTimeMillis())), true, null, 0);
            mChatList.add(chat);
            messageViewIntent.putExtra("model-pointer", model);
            messageViewIntent.putExtra("chat", mChatList.size()-1);
            saveChatList();
            messageViewIntent.putExtra("chatList", (Serializable) mChatList);
            startActivity(messageViewIntent);
            mAdapter.setChatList(mChatList);
            mAdapter.notifyItemInserted(mChatList.size());
//            finish();
        });
        mAdapter = new ChatAdapter(this, mChatList, this, this);
        mRecyclerView.setAdapter(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(mAdapter));
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

    }



    static {
        System.loadLibrary("llmodel");
        System.loadLibrary("gptj-default");
    }


    // Random stuff for the chat interface
    private void saveChatList() {
        // Write the chat list to a file
        try {
            FileOutputStream fos = openFileOutput("chat_list.ser", MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mChatList);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChatList() {
        // Read the chat list from a file
        try {
            FileInputStream fis = openFileInput("chat_list.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            mChatList = (ArrayList<Chat>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
//            Toast.makeText(this, "Load failed", Toast.LENGTH_SHORT).show();
        }

        // Initialize the chat list if it doesn't exist
        if (mChatList == null) {
            mChatList = new ArrayList<>();
        }
    }

    @Override
    public void clicked(int position) {
        if(model!=0){
            Chat item = mChatList.get(position);
            // Create an Intent to start a new activity
            Intent intent = new Intent(MainActivity.this, MessageView.class);
            intent.putExtra("model-pointer", model);
            intent.putExtra("chat", position);
            intent.putExtra("old-chat", true);
            intent.putExtra("chatList", (Serializable) mChatList);
            // Start the new activity
            startActivity(intent);
        }
    }

    @Override
    public void deleteItem(int position) {
        ArrayList<Chat> temp = new ArrayList<>(mChatList.size());
        for(int i = 0; i < mChatList.size(); i++){
            if(i == position){
            } else if (i>position){
                Chat chat = mChatList.get(i);
                temp.add(chat);
            } else {
                temp.add(mChatList.get(i));
            }
        }
        mChatList = temp;
        saveChatList();
        mAdapter.setChatList(mChatList);
        mAdapter.notifyItemRemoved(position);
    }

    public static void createEmptyTextFile() {
        // Specify the directory path
        String directoryPath = "/storage/emulated/0/Android/data/com.lucario.gpt4allandroid/files/Documents";

        // Create a File object with the specified directory path and file name
        File file = new File(directoryPath, "response.txt");

        try {
            // Check if the file exists
            if (file.exists()) {
                // Delete the existing file
                file.delete();
            }

            // Create the empty text file
            if (file.createNewFile()) {
                System.out.println("Empty text file created successfully.");
            } else {
                System.out.println("Error occurred while creating the empty text file.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the empty text file.");
            e.printStackTrace();
        }
    }

    private  AlertDialog Ddialog;
    private void showDownload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Downloading Model")
                .setMessage("Please wait while the Model is being downloaded.\n Check notif for progress");

        Ddialog = builder.create();
        Ddialog.setCancelable(false);
        Ddialog.show();
    }


    private void showLoading() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Loading Model")
                .setMessage("Please wait while the Model is being loaded");

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
       new Thread(new Runnable() {
           @Override
           public void run() {
               while(model==0){}
              runOnUiThread(() -> {dialog.setCancelable(true); dialog.dismiss(); newChatButton.setEnabled(true);});

           }
       }).start();
        dialog.show();
    }

    public static void updateModel(int numThreads){
        destoryModel(model);
        new Thread(() -> {
            createEmptyTextFile();
            model = loadGo(modelPath, numThreads);
        }).start();
    }
}