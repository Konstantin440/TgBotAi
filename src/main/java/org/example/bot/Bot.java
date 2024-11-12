package org.example.bot;


import lombok.extern.slf4j.Slf4j;
import org.example.service.MessageService;
import org.example.service.quartz.RateLimitService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Slf4j


public class Bot extends TelegramLongPollingBot {

    public Map<Long, List<String>> getHistory() {
        return history;
    }

    private Map<Long, List<String>> history = new HashMap<>();


    private RateLimitService rateLimitService = new RateLimitService();

    public Bot(String botToken) {
        super(botToken);

    }


    public void addMessageInHistory(Update update) {
        if (update.getMessage() != null) {
            Long chatId = update.getMessage().getChatId();
            String userMessage = update.getMessage().getText();

            if (userMessage != null && !userMessage.trim().isEmpty()) {
                // Получаем или создаем новый список сообщений для данного chatId
                List<String> messages = history.computeIfAbsent(chatId, k -> new ArrayList<>());

                // Добавляем новое сообщение в список
                messages.add(userMessage);
            }
        }
    }

    //TEST1
    private void writeMessageToFile(Long id, String msg) {
        String currentDirectory = System.getProperty("user.dir");
        String filePath = currentDirectory + File.separator + "id_" + id + ".txt"; //как получить логин?

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write("Пользователь с id " + id + ": " + msg); // Запись ID и сообщения
            writer.newLine(); // Новая строка
            log.info("Сообщение записано в файл: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onUpdateReceived(Update update)   {
        Long chatId = update.getMessage().getChatId();

        if (rateLimitService.getLimit(chatId)>=2) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId());
            message.setText("Вы достигли лимита в 30 сообщений на сегодня. Попробуйте завтра c 8:00 или напише Косте!!!!$$$$$$$");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;

        }else {
            rateLimitService.canMakeRequest(chatId); //если лимит не достигнут то увеличить его на 1
            //добавить его в мап если его там нету а ели есть то сменить ему лемит на 1 больше текущего
        }


        System.out.println(update.getMessage().getMessageThreadId());
        MessageService messageService = new MessageService();
        String userMessage = update.getMessage().getText(); // то что вел пользователь


        String messageResponse = messageService.getMessageResponse(update.getMessage().getText());
        System.out.println("response " + messageResponse);
        addMessageInHistory(update);
        writeMessageToFile(chatId, userMessage);


        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(messageResponse);


        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getBotUsername() {
        return "java_tasks_v1_bot";
    }
}
