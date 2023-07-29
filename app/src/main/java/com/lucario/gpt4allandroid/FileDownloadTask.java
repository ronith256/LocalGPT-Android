package com.lucario.gpt4allandroid;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class FileDownloadTask extends AsyncTask<String, Integer, String> {
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private ProgressBar progressBar;
    private Dialog progressDialog;

    private TextView progressText;

    public FileDownloadTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new Dialog(context);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false);
        progressBar = progressDialog.findViewById(R.id.progressBar);
        progressText = progressDialog.findViewById(R.id.percent);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        String fileUrl = params[0];
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            double fileLength = 3785248281d;

            BufferedInputStream input = new BufferedInputStream(url.openStream());
            FileOutputStream output = new FileOutputStream(new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ggml-gpt4all-j-v1.3-groovy.bin").getPath());

            byte[] data = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data,0,1024)) != -1) {
                output.write(data, 0, count);
                total += count;
                long finalTotal = total;
                publishProgress((int) ((finalTotal * 100) / fileLength));
            }

            output.flush();
            output.close();
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileName;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        // Update the progress bar with the new value
        int progress = values[0];
        progressBar.setIndeterminate(false);
        progressBar.setProgress(progress,true);
        progressText.setText(String.valueOf(progress) + "%");
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        progressDialog.dismiss();
    }
}


