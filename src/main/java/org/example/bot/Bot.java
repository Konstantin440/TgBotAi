package org.example.bot;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.model.ChatResponse;
import org.example.service.MessageService;
import org.example.service.quartz.RateLimitService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

@Slf4j
public class Bot extends TelegramLongPollingBot {
    private String messageResponse;
    private RateLimitService rateLimitService = new RateLimitService();
    private Map<Long, List<String>> historyUser = new HashMap<>();


    public Bot(String botToken) {
        super(botToken);

    }

    public void addMessageInHistory(Update update) {
        if (update.getMessage() != null) {
            Long chatId = update.getMessage().getChatId();
            String userMessage = update.getMessage().getText();
            if (userMessage != null && !userMessage.trim().isEmpty()) {
                List<String> messages = historyUser.computeIfAbsent(chatId, k -> new ArrayList<>());
                messages.add(userMessage);
            }
        }
    }

    private void writeMessageToFile(Long chatId, String msg) {
        String currentDirectory = System.getProperty("user.dir");
        String filePath = currentDirectory + File.separator + "id_" + chatId + ".txt"; //как получить логин?

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write("Пользователь с id " + chatId + ": " + msg); // Запись ID и сообщения
            writer.newLine(); // Новая строка
            log.info("Сообщение записано в файл: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onUpdateReceived(Update update) {
        boolean isThereAMessage = update.hasMessage() && update.getMessage() != null;
        if (isThereAMessage) {

        Long chatIdLimit = update.getMessage().getChatId();

        if (rateLimitService.getLimit(chatIdLimit)>=2) {
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
            rateLimitService.canMakeRequest(chatIdLimit); //если лимит не достигнут то увеличить его на 1
//добавить его в мап если его там нету а ели есть то сменить ему лемит на 1 больше текущего
        }



        // Проверяем наличие сообщения

            boolean isText = update.getMessage().hasText();

            if (isText) {
                Long id = update.getMessage().getChatId();
                MessageService messageService = new MessageService();
                String userMessage = update.getMessage().getText(); // то что ввел пользователь

                // Получаем сообщение от сервиса
                messageResponse = messageService.getMessageResponse(userMessage);
                writeMessageToFile(id, messageResponse);
                addMessageInHistory(update);
                sendMessage(id, messageResponse);
            }
        }
        boolean pressButton = update.hasCallbackQuery() && update.getCallbackQuery().getData() != null;

        if (pressButton) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId(); // Получаем chatId из сообщения, связанного с кнопкой
            Integer idMsg = update.getCallbackQuery().getMessage().getMessageId();

            // Отправляем временное сообщение для информирования о том, что процесс запущен
            sendMessage(chatId, "Обработка, пожалуйста, подождите... ⏳");
            sendEditMessage(update, chatId, idMsg, messageResponse);
        }
    }

    public void sendEditMessage(Update update, Long chatId, Integer msgId, String messageResponse) {
        new Thread(() -> {
            try {
                // Ждем обработки задачи
                String responseText = getASolution(update, messageResponse);

                // Редактируем предыдущее сообщение, используя EditMessageText
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(msgId + 1);
                editMessageText.setText(responseText);  // Устанавливаем текст с ответом
                editMessageText.setParseMode("Markdown");
                // Выполняем редактирование сообщения
                execute(editMessageText);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    public String getASolution(Update update, String task) {
        String showSolution = "show_solution";
        String answer = "";


        // Поскольку мы уже удостоверились, что есть CallbackQuery, просто извлекаем данные
        String namePressedButton = update.getCallbackQuery().getData();

        // Проверяем, является ли нажатая кнопка нужной
        if (namePressedButton.equals(showSolution)) {
            MessageService messageService = new MessageService();

            String text = "$ пришли решение задачи на языке java. добавь комментарии на каждую строку о том что мы делаем." +
                    task.replace("\"", "")
                            .replace("\n", "");
            answer = messageService.getMessageResponse(text);
            log.info(text);
            log.info(answer);
        }
        return answer;
    }

    @SneakyThrows
    public void sendMessage(Long id, String messageResponse) {
        SendMessage sendMessage = createSendMessage(id, messageResponse);
        if (isTask(messageResponse)) {
            attachButtonToMessage(sendMessage, id);
        }
        execute(sendMessage);
    }

    private SendMessage createSendMessage(Long id, String messageResponse) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);
        sendMessage.setText(messageResponse);
        return sendMessage;
    }

    private boolean isTask(String messageResponse) {
        return messageResponse.toLowerCase().startsWith("задача");
    }

    private void attachButtonToMessage(SendMessage sendMessage, Long id) {
        Button button = new Button();
        sendMessage.setReplyMarkup(button.sendButton(id).getReplyMarkup());
    }

    @Override
    public String getBotUsername() {
        return "java_tasks_v1_bot";
    }


}



