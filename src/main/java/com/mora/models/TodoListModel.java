package com.mora.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class TodoListModel {

    private String todoId;
    private String task;
    private boolean isCompleted;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("todoId")
    public String getTodoId(){
        return this.todoId;
    }


    public String getTask() {
        return task;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setTodoId(String todoId) {
        this.todoId = todoId;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
