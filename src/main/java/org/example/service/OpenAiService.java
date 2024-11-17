package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.ChatResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class OpenAiService {
    String url = "https://api.proxyapi.ru/openai/v1/chat/completions";
    final String apiKey = "sk-vIz7Q9PumU1oqmcjYrwKb9dTlFxlUlHO"; // API key goes here s
    String model = "gpt-3.5-turbo"; // current model of chatgpt api


    private final String PREFIX_TASK = "Напиши задачу на java без решения. Ответ всегда начинай со слова \\\"задача\\\". " +
            "Подробнее опиши по пунктам ход действий. Тема задачи: ";


    public String getMessage(String typeMessage, String readyRequest) {
        String responseAi = "";
        String requestForAi = "";

        switch (typeMessage) {
            case "*" -> requestForAi = PREFIX_TASK + readyRequest;
            default -> requestForAi = readyRequest;
        }

        responseAi = sendRequest(requestForAi);

        return responseAi;
    }


    public String sendRequest(String readyRequest) {
        try {
            URL objURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) objURL.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            con.setRequestProperty("Content-Type", "application/json");

            // Формируем JSON
            String jsonString = getJsonString(model, readyRequest);
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Проверяем код ответа
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + jsonString);
            System.out.println("Response Code : " + responseCode);

            // Читаем ответ от API
            InputStream inputStream = (responseCode == HttpURLConnection.HTTP_OK) ? con.getInputStream() : con.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String collectJson = br.lines().collect(Collectors.joining("\n"));

            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("Ошибка: " + collectJson); // Выводим ошибку в консоль
            }

            return getTextMessageFromJson(collectJson);



        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // В случае ошибки вернуть null
    }


    public String getTextMessageFromJson(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ChatResponse chatResponse = objectMapper.readValue(json, ChatResponse.class);
            return chatResponse.getMessageResponse();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }


    private String getJsonString(String model, String message) {
        String body =
                """
                        {
                            "model": "%s",
                            "messages": [
                                {
                                    "role": "user",
                                    "content": "%s"
                                }
                            ],
                            "temperature": 0.7
                        }
                        """.formatted(model, message).trim();
        return body;
    }


}
