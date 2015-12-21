package com.todo.codethatkills.todolist;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.todo.codethatkills.todolist.Activities.MainActivity;
import com.todo.codethatkills.todolist.Fragments.TaskFragment;

import java.util.ArrayList;
import java.util.List;

public class UnitsAdapter extends RecyclerView.Adapter<UnitsAdapter.MyViewHolder> {
    List<TaskItem> data = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;

    public UnitsAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.organization_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        TaskItem current = data.get(position);
        holder.title.setText(current.getName());
        holder.title.setOnClickListener(clickListener);
        holder.title.setTag(holder);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public void swapData(List<TaskItem> newOrganizations) {
        this.data = newOrganizations;
        notifyDataSetChanged();
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MyViewHolder holder = (MyViewHolder) view.getTag();
            int position = holder.getAdapterPosition();

            TaskItem taskItem = data.get(position);
            try{
                final MainActivity activity = (MainActivity) context;
                FragmentManager manager = activity.getSupportFragmentManager();
                TaskFragment fragment = TaskFragment.newInstance(taskItem);
                String tag = fragment.getClass().getSimpleName();
                fragment.show(manager, tag);
            } catch (ClassCastException e) {
//                Log.d(TAG, "Can't get the fragment manager with this");
            }
        }
    };

    public TaskItem getItem(int position) {
        return data == null ? null : data.get(position);
    }

    public void remove(String text) {
        //TODO implement
    }

    public void add(TaskItem item) {
        data.add(item);
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.shortTitle);
        }
    }
}