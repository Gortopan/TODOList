package com.todo.codethatkills.todolist.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.todo.codethatkills.todolist.Fragments.AddTaskFragment;
import com.todo.codethatkills.todolist.R;
import com.todo.codethatkills.todolist.TaskItem;
import com.todo.codethatkills.todolist.UnitsAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String DB_URI = "https://codethatkillstodo.firebaseio.com/tasks";

    private Firebase ref;
    private UnitsAdapter adapter;
    private ProgressBar pb;
    public static String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        pb = (ProgressBar) findViewById(R.id.mainProgressBar);
        pb.setVisibility(View.VISIBLE);
        adapter = new UnitsAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (!Firebase.getDefaultConfig().isPersistenceEnabled()){
            Firebase.getDefaultConfig().setPersistenceEnabled(true);
        }
        Firebase.setAndroidContext(this);
        Firebase loginRef = new Firebase(DB_URI);
        SharedPreferences myPrefs = this.getSharedPreferences(getString(R.string.token), Context.MODE_PRIVATE);
        String token = myPrefs.getString(getString(R.string.token), "");
        if (token.equals(""))logOut();
        loginRef.authWithOAuthToken("google", token, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                userId = authData.getUid();

                ref = new Firebase(generateUserDBUri());
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        ArrayList<TaskItem> items = new ArrayList<>();
                        for (DataSnapshot item : snapshot.getChildren()) {
                            TaskItem taskItem = item.getValue(TaskItem.class);
                            items.add(taskItem);
                        }
                        adapter.swapData(items);
                        pb.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        pb.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // there was an error
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddTaskFragment fragment = new AddTaskFragment();
                fragment.show(getSupportFragmentManager(), "add");
            }
        });
    }

    public static String generateUserDBUri(){
        return DB_URI + "users/" + userId;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void logOut(){
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.token), Context.MODE_PRIVATE).edit();
        editor.putString(getString(R.string.token), "");
        editor.commit();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
