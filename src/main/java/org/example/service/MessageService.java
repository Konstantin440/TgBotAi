package org.example.service;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class MessageService {
    private OpenAiService openAiService;


    public MessageService() {
        this.openAiService = new OpenAiService();
    }


    // Метод для получения ответа на запрос пользователя
    public String getMessageResponse(String messageRequest) {
        // Проверяем, начинается ли текст запроса с "*"
        if (messageRequest.startsWith("*")) {
            String readyRequest = generateTask("*", messageRequest); // Генерируем задачу со звездочкой. (для задачи)
            return readyRequest; // Возвращаем готовую задачу.
        }

        // Проверяем, начинается ли текст запроса с "$"
        if (messageRequest.startsWith("$")) {
            String readyRequest = generateTask("$", messageRequest); // Генерируем задачу с долларом. (для ответа на задачу)
            return readyRequest; // Возвращаем готовую задачу.
        }

        String responseMessage = "";
        switch (messageRequest) {
            case "/start" -> responseMessage = startText(); // Если запрос "/start", генерируем текст приветствия.
            default -> responseMessage = defaultText(); // В противном случае генерируем текст по умолчанию.
        }
        return responseMessage; // Возвращаем ответное сообщение.
    }

    // Метод для генерации задачи на основе типа и запроса
    public String generateTask(String type, String readyRequest) {
        return openAiService.getMessage(type, readyRequest); // Используем OpenAiService для получения сообщения.
    }

    // Метод для текста по умолчанию
    private static String defaultText() {
        return "(Возможно вы забыли поставить * перед названием)\nПример: *Массивы"; // Сообщение о неверном вводе.
    }

    // Метод для текста приветствия
    private static String startText() {
        StringBuilder stringBuilder = new StringBuilder(); // Строим строку с помощью StringBuilder.
        stringBuilder.append("Задачник по java\n"); // Добавляем текст приветствия.
        stringBuilder.append("Что бы получить задачу просто напиши * далее тему").append("\n"); // Инструкция по получению задачи.
        stringBuilder.append("Пример: *Массивы"); // Пример запроса задачи.
        return stringBuilder.toString(); // Возвращаем собранный текст.
    }

    // Метод для получения решения задачи
    public String getASolution(String task) {
        String answer = "";
        String text = "$ пришли решение задачи на языке java. добавь подробные комментарии на каждую строку о том, что мы делаем." +
                task.replace("\"", "")
                        .replace("\n", ""); // Заменяем кавычки и новые строки для чистоты запроса
        // чтобы модель могла прочитать его

        answer = getMessageResponse(text); // Получаем ответ по запросу решения задачи.
        log.info(text); // Логируем текст запроса в gpt
        log.info(answer); // Логируем ответ gpt
        return answer;
    }
}



