package org.example.bot;


import lombok.extern.slf4j.Slf4j;
import org.example.service.MessageService;
import org.springframework.stereotype.Component;
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
@Component

public class Bot extends TelegramLongPollingBot {

    public Map<Long, List<String>> getHistory() {
        return history;
    }

    private Map<Long, List<String>> history = new HashMap<>();


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

    public boolean countMessageInHistory(Long chatId) {
        // Получаем список сообщений для данного chatId
        List<String> messages = history.get(chatId);

        // Проверяем, если список существует и его размер больше 5
        return messages != null && messages.size() == 30;
    }


    @Override
    public void onUpdateReceived(Update update) {
        Long id = update.getMessage().getChatId();

        if (countMessageInHistory(id)) {
            SendMessage sendMessageFailled = new SendMessage();
            sendMessageFailled.setChatId(id);
            sendMessageFailled.setText("Вы достигли лимита в 30 сообщений на сегодня");


            try {
                execute(sendMessageFailled);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        System.out.println(update.getMessage().getMessageThreadId());
        MessageService messageService = new MessageService();
        String userMessage = update.getMessage().getText(); // то что вел пользователь


        String messageResponse = messageService.getMessageResponse(update.getMessage().getText());
        System.out.println("response " + messageResponse);
        addMessageInHistory(update);
        writeMessageToFile(id, userMessage);


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
