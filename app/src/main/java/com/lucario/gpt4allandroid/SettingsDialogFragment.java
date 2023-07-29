package com.lucario.gpt4allandroid;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class SettingsDialogFragment extends DialogFragment {

    private EditText contextSizeEditText;

    private EditText numThreadsEditText;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a new AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // Inflate the custom layout for the dialog
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_settings, null);

        // Find the EditText and Button views in the layout
        contextSizeEditText = view.findViewById(R.id.session_time_edit_text);
        numThreadsEditText = view.findViewById(R.id.http_timeout_edit_text);
        setValues();
        Button saveButton = view.findViewById(R.id.save_button);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("nums", MODE_PRIVATE);

       saveButton.setOnClickListener(e->{
           try{
               sharedPreferences.edit().putInt("ctx-size", Integer.parseInt(contextSizeEditText.getText().toString())).apply();
               sharedPreferences.edit().putInt("num-threads", Integer.parseInt(numThreadsEditText.getText().toString())).apply();
//               MainActivity.updateModel(sharedPreferences.getInt("num-threads", 4));
           } catch(Exception ignored){
               sharedPreferences.edit().putInt("ctx-size", 2048).apply();
               sharedPreferences.edit().putInt("num-threads", 4).apply();
           }
       });
        // Set the custom layout for the dialog and return it
        builder.setView(view);
        return builder.create();
    }

    private void setValues(){
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("nums", MODE_PRIVATE);
        contextSizeEditText.setText(String.valueOf(sharedPreferences.getInt("ctx-size", 2048)));
        numThreadsEditText.setText(String.valueOf(sharedPreferences.getInt("num-threads", 4)));
    }
}

