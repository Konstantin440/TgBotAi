package org.example.service;

import lombok.SneakyThrows;
import org.example.bot.Bot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;


public class MessageSender {

    // Хранит экземпляр бота, который будет использоваться для отправки сообщений
    private final Bot bot;


    public MessageSender(Bot bot) {
        this.bot = bot; //
    }

    // Метод для отправки сообщения и возвращения идентификатора сообщения (нужен для того чтобы редактировать нужное смс)
    @SneakyThrows // исключения
    public Integer sendMessageAndReturnId(Long id, String messageResponse) {
        // Создаем объект SendMessage, используя метод createSendMessage класса Bot
        SendMessage sendMessage = bot.createSendMessage(id, messageResponse);
        return bot.execute(sendMessage).getMessageId();     // Отправляем сообщение и возвращаем идентификатор сообщения
    }

    @SneakyThrows
    // Метод для отправки сообщения без возврата идентификатора
    public void sendMessage(Long id, String messageResponse) {
        SendMessage sendMessage = bot.createSendMessage(id, messageResponse);// Создаем объект SendMessage

        if (bot.isTask(messageResponse)) {  // Проверяем, является ли сообщение задачей, и если да, то прикрепляем кнопку к сообщению
            bot.getButtonService().attachButtonToMessage(sendMessage, id);
        }
        bot.execute(sendMessage);
    }

    // Метод для редактирования двух сообщений
    // (удаляем кнопку у нашей задачи, ведь она уже нажата, вместо уведомления присылаем решение)
    @SneakyThrows // А
    public void sendEditMessages(Long chatId, Integer messageId, String responseText, String task, Integer taskId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId); // Устанавливаем идентификатор чата
        editMessageText.setMessageId(messageId); // Устанавливаем идентификатор редактируемого сообщения
        editMessageText.setText(responseText); // Устанавливаем текст с решением
        editMessageText.setParseMode("Markdown"); // Указываем режим специальной разметки в телеграмме для красивого кода

        // Создаем второй объект для редактирования сообщения задачи
        EditMessageText editMessageText1 = new EditMessageText();
        editMessageText1.setChatId(chatId);
        editMessageText1.setMessageId(taskId);
        editMessageText1.setText(task); // Устанавливаем текст задачи
        editMessageText1.setReplyMarkup(null); // Убираем кнопки у сообщения задачи
        bot.execute(editMessageText);   // Отправляем редактирование первого сообщения (решения)
        bot.execute(editMessageText1); // Отправляем редактирование второго сообщения (задачи)
    }
}
