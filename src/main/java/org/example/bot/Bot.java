package org.example.bot;

import lombok.Data;
import org.example.service.ButtonService;
import org.example.service.MessageSender;
import org.example.service.MessageService;
import org.example.service.quartz.RateLimitService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Bot extends TelegramLongPollingBot {

    private final MessageService messageService;
    private final ButtonService buttonService;
    private final RateLimitService rateLimitService;
    private final MessageSender messageSender;
    private Map<Long, String> userTasks = new ConcurrentHashMap<>();
    private String messageResponse;

//Передаем this в конструкторы ButtonService и MessageSender, для связи между экземплярами объекта Bot и других сервисов,
// необходимо для их корректного функционирования и взаимодействия телеграмм бота.

    public Bot(String botToken) {
        super(botToken);
        this.rateLimitService = new RateLimitService();
        this.messageService = new MessageService();
        this.buttonService = new ButtonService(this);
        this.messageSender = new MessageSender(this);
    }

    @Override
    public String getBotUsername() {
        return "java_tasks_v1_bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("update:" + update.hashCode());

        //проверка на лимиты
        if (isLimit(update)) return;

        //обработка текста
        textHandler(update);

        //ручка нажатия кнопки
        buttonService.handleButtonPress(update, messageService, messageSender);

    }

    private void textHandler(Update update) {

        boolean isMessageAndText = update.hasMessage() && update.getMessage().hasText();
        if (isMessageAndText) {  // Проверяем, содержит ли обновление сообщение и есть ли текст в этом сообщении
            Long id = update.getMessage().getFrom().getId();   // Получаем идентификатор пользователя, который отправил сообщение
            String userMessage = update.getMessage().getText(); // Получаем текст сообщения, отправленного пользователем
            MessageService messageService = new MessageService();  // Создаем MessageService для обработки сообщения
            messageResponse = messageService.getMessageResponse(userMessage); //ответ бота с задачей
            userTasks.put(id, messageResponse);// Сохраняем ответ пользователя в хранилище задач по идентификатору пользователя
            messageSender.sendMessage(id, messageResponse);   // Отправляем сформированный ответ обратно пользователю
        }
    }

    //создание сообщения
    public SendMessage createSendMessage(Long id, String messageResponse) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);
        sendMessage.setText(messageResponse);
        return sendMessage;
    }

    //проверка на лимиты
    public boolean isLimit(Update update) {
        if (!(update.hasMessage() && update.getMessage().hasText())) return false;
        Long chatId = update.getMessage().getChatId();

        if (rateLimitService.getLimit(chatId) >= 30) {
            messageSender.sendMessage(chatId, "Вы достигли лимита в 30 сообщений на сегодня. Лимиты обновляются ежедневно в 21:00 по МСК ");
            return true;
        } else {
            rateLimitService.canMakeRequest(chatId);
        }
        return false;
    }

    //проверка является ли сообщение задачей (если задача то кнопка добавится)
    //В запросе просил бота все сообщения начинать со слова задача, приравняв текст к нижнему регистру
    // (иногда он присылает "задача" с большой буквы)
    public boolean isTask(String messageResponse) {
        return messageResponse.toLowerCase().startsWith("задача");
    }

}