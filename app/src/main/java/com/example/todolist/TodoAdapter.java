package com.example.todolist;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;


import java.util.ArrayList;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(TodoItem task);
    }

    private ArrayList<TodoItem> taskList;
    private OnTaskClickListener clickListener;

    public TodoAdapter(ArrayList<TodoItem> taskList, OnTaskClickListener clickListener) {
        this.taskList = taskList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_item, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TodoViewHolder extends RecyclerView.ViewHolder {
        // Views in item_task layout
        private TextView tvTitle, tvDateTime;
        private ImageView ivThumbnail;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.todoTitle);
            tvDateTime = itemView.findViewById(R.id.todoDateTime);
            ivThumbnail = itemView.findViewById(R.id.todoThumbnail);
        }

        public void bind(TodoItem task) {
            tvTitle.setText(task.getTitle());
            tvDateTime.setText(task.getDateTime());

            // Load image using Glide or placeholder if null
            if (task.getImageUri() != null) {
                Glide.with(itemView.getContext()).load(task.getImageUri()).into(ivThumbnail);
            } else {
                ivThumbnail.setImageResource(R.drawable.placeholder_image);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTaskClick(task);
                }
            });
        }
    }
}
