package com.todo.codethatkills.todolist.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.todo.codethatkills.todolist.Activities.MainActivity;
import com.todo.codethatkills.todolist.R;
import com.todo.codethatkills.todolist.TaskItem;

public class AddTaskFragment extends DialogFragment {

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = View.inflate(getActivity(), R.layout.add_task_fragment, container);
        final EditText taskText = (EditText) rootView.findViewById(R.id.taskEditText);
        Button addButton = (Button) rootView.findViewById(R.id.addTaskButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!taskText.getText().toString().isEmpty()){
                    Firebase ref = new Firebase(MainActivity.generateUserDBUri());
                    TaskItem taskItem = new TaskItem(taskText.getText().toString());
                    ref.push().setValue(taskItem);
                    dismiss();
                }
            }
        });
        return rootView;
    }
}
