package com.todo.codethatkills.todolist;

import java.io.Serializable;

public class TaskItem implements Serializable{

    private String name;

    public TaskItem(){}

    public TaskItem(String name){
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
