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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.todo.codethatkills.todolist.Activities.MainActivity;
import com.todo.codethatkills.todolist.R;
import com.todo.codethatkills.todolist.TaskItem;

import java.util.HashMap;
import java.util.Map;

public class TaskFragment extends DialogFragment{
    private static final String TASK_KEY = "task";
    private TaskItem currentTaskItem;
    private EditText editText;
    private Button editButton;
    private boolean editMode;

    public static TaskFragment newInstance(TaskItem taskItem) {
        TaskFragment f = new TaskFragment();
        if (taskItem != null){
            Bundle args = new Bundle();
            args.putSerializable(TASK_KEY, taskItem);
            f.setArguments(args);
        }
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null){
            currentTaskItem = (TaskItem) args.getSerializable(TASK_KEY);
        }
    }

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
        View rootView = inflater.inflate(R.layout.task_fragment, container);
        editText = (EditText) rootView.findViewById(R.id.editTaskText);
        editButton = (Button) rootView.findViewById(R.id.editButton);
        Button closeButton = (Button) rootView.findViewById(R.id.closeButton);
        Button removeButton = (Button) rootView.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Firebase(MainActivity.generateUserDBUri())
                        .orderByChild(getString(R.string.name))
                        .equalTo(currentTaskItem.getName())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()) {
                                    DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                    firstChild.getRef().removeValue();
                                }
                                dismiss();
                            }

                            public void onCancelled(FirebaseError firebaseError) {
                                dismiss();
                            }
                        });
            }
        });
        editMode = false;
        editText.setText(currentTaskItem.getName());
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editMode){
                    editMode = true;
                    editText.setFocusableInTouchMode(true);
                    editButton.setText(getString(R.string.apply));
                }else {
                    editText.setFocusableInTouchMode(false);
                    new Firebase(MainActivity.generateUserDBUri())
                            .orderByChild(getString(R.string.name))
                            .equalTo(currentTaskItem.getName())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChildren()) {
                                        DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                        Map<String, Object> updateName = new HashMap<>();
                                        updateName.put(getString(R.string.name), editText.getText().toString());
                                        firstChild.getRef().updateChildren(updateName);
                                        currentTaskItem = new TaskItem(editText.getText().toString());
                                    }
                                }

                                public void onCancelled(FirebaseError firebaseError) {
                                }
                            });
                    editMode = false;
                    editText.setFocusableInTouchMode(false);
                    editButton.setText(getString(R.string.edit));
                }
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return rootView;
    }
}
