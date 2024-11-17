package org.example.bot;


import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class Button {


    // Метод для добавления кнопки "Показать решение"
    public  SendMessage sendButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Твоя кнопка");
        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createBtn("Показать решение", "show_solution")));
        inlineKeyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;

    }

    public  InlineKeyboardButton createBtn(String name, String data) {
        var inline = new InlineKeyboardButton();
        inline.setText(name);
        inline.setCallbackData(data);
        return inline;
    }
}