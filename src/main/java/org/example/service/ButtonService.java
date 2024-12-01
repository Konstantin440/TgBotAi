package org.example.service;


import org.example.bot.Bot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class ButtonService {
    private Bot bot;

    public ButtonService(Bot bot) {
        this.bot = bot;
    }

    // Метод для создания кнопки (inline-кнопки)
    public InlineKeyboardButton createBtn(String name, String data) {
        var inline = new InlineKeyboardButton();
        inline.setText(name);  // Устанавливаем текст кнопки
        inline.setCallbackData(data); // Устанавливаем данные обратного вызова для кнопки
        return inline;
    }

    // Метод для создания сообщения с кнопкой "Показать решение"
    public SendMessage sendButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);    // Устанавливаем идентификатор чата для сообщения

        var inlineKeyboardMarkup = new InlineKeyboardMarkup(); // Создаем разметку клавиатуры для кнопок
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createBtn("Показать решение", "show_solution")));   // Добавляем кнопку к клавиатуре
        inlineKeyboardMarkup.setKeyboard(keyboard); // Устанавливаем созданную клавиатуру в разметку сообщения
        message.setReplyMarkup(inlineKeyboardMarkup);   // Устанавливаем разметку клавиатуры в сообщение
        return message; // Возвращаем сообщение с кнопкой
    }

    public void attachButtonToMessage(SendMessage sendMessage, Long id) {
        ButtonService button = new ButtonService(this.bot) ;
        sendMessage.setReplyMarkup(button.sendButton(id).getReplyMarkup()); //добавления кнопки к сообщению
    }


    // Метод для обработки нажатий на кнопки
    public void handleButtonPress(Update update, MessageService messageService, MessageSender messageSender) {
        // Проверяем, есть ли у обновления (отправленного пользователем) данные обратного вызова
        boolean pressButton = update.hasCallbackQuery() && update.getCallbackQuery().getData() != null;

        // Если кнопка была нажата
        if (pressButton) {
            // Получаем ID пользователя, который нажал на кнопку
            Long userId = update.getCallbackQuery().getFrom().getId();
            // Получаем ID чата, в котором было нажатие
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            // Получаем ID сообщения, к которому привязана кнопка
            Integer msgId = update.getCallbackQuery().getMessage().getMessageId();

            // Отправляем уведомление об обработке запроса
            Integer messageId = messageSender.sendMessageAndReturnId(chatId, "Обработка, пожалуйста, подождите... ⏳");

            // Получаем задачу пользователя, используя его ID ( из хранилища задач который статический и принадлежит классу BOT)
            String task = bot.getUserTasks().get(userId);

            // Создаем новый поток для обработки решения задачи
            new Thread(() -> {
                // Получаем текст решения для задачи
                String responseText = messageService.getASolution(task);
                // Обновляем сообщение с решением задачи
                messageSender.sendEditMessages(chatId, messageId, responseText, task, msgId);
            }).start(); // Запускаем поток
        }
    }
}

