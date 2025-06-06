package com.mora;


import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.mora.models.TodoListModel;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;


/**
 * Handler for requests to Lambda function.
 */
public class TodoList implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbTable<TodoListModel> userTable;
    private  LambdaLogger logger;
    private final Gson gson;

    public TodoList() {
        DynamoDbClient dynamoDbClient =  DynamoDbClient.create();

        DynamoDbEnhancedClient build = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

       userTable = build.table("TodoList", TableSchema.fromBean(TodoListModel.class));
       gson = new Gson();
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        logger = context.getLogger();


        String httpMethod = input.getHttpMethod();

        switch (httpMethod){
            case "GET":
                return getAllTodoList(input);
            case "POST":
                return saveTodoListItem(input);
            case "PUT":
                return updateTask(input);
            case "DELETE":
                return deleteTask(input);
            default:
                return new APIGatewayProxyResponseEvent().withBody("Method not allowed yet working on that :)");
        }
    }

    //TODO we have not test this method yet
    private APIGatewayProxyResponseEvent deleteTask(APIGatewayProxyRequestEvent input) {
        String todoId = input.getPathParameters().getOrDefault("todoId", "");
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        try {
            TodoListModel todoListModel = userTable.deleteItem(Key.builder().partitionValue(todoId).build());
            return responseEvent.withBody(gson.toJson(todoListModel));
        } catch (Exception e) {
            return responseEvent.withBody("There is an exception" + e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent updateTask(APIGatewayProxyRequestEvent input) {
        String todoId = input.getPathParameters().getOrDefault("todoId", "");
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        if(todoId.isEmpty()) return responseEvent.withBody("You need to provide an ID");
        try {
            TodoListModel item = userTable.getItem(Key.builder().partitionValue(todoId).build());
            item.setCompleted(true);
            userTable.putItem(item);
            return responseEvent.withBody(gson.toJson(item));
        }catch (Error ex){
            return responseEvent.withBody("Something went wrong " + ex.getMessage());
        }
    }


    private APIGatewayProxyResponseEvent getAllTodoList(APIGatewayProxyRequestEvent input) {

        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        String todoId = input.getPathParameters().getOrDefault("todoId", "");
        try {

            //We are fetching here all items
            if(todoId.isEmpty()){
                List<TodoListModel> result = new ArrayList<>();
                userTable.scan(ScanEnhancedRequest.builder().build())
                        .items()
                        .forEach(result::add);

                return responseEvent.withBody(gson.toJson(result));
            }

            //We are fetching a single item
            TodoListModel item = userTable.getItem(Key.builder().partitionValue(todoId).build());
            return responseEvent.withBody(gson.toJson(item));

        }catch (Error e){
            logger.log("Could not get information :(" + e.getMessage());
            return responseEvent.withBody("Something went wrong please check the logs");
        }

    }

    private APIGatewayProxyResponseEvent saveTodoListItem(APIGatewayProxyRequestEvent input){
        String body = input.getBody();
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        try {
            TodoListModel todoList = gson.fromJson(body, TodoListModel.class);
            userTable.putItem(todoList);
            return responseEvent.withBody(body);
        }catch (Error e){
            logger.log("Something went wrong trying to save the item :( " + e.getMessage());
            return responseEvent.withBody("Something went wrong trying to save the item" + e.getMessage());
        }
    }

}
