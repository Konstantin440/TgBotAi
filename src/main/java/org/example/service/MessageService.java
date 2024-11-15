package org.example.service;

public class MessageService {
    private OpenAiService openAiService;

    public MessageService() {
        this.openAiService = new OpenAiService();
    }

    public String getMessageResponse(String messageRequest) {


        if (messageRequest.startsWith("*") ) {
            String readyRequest = generateTask("*",messageRequest);
            return readyRequest;
        }

        String responseMessage="";
        switch (messageRequest){
            case "/start"-> responseMessage = startText();
            default ->  responseMessage = defaultText();

        }
        return responseMessage;
    }


    private String generateTask(String type,String readyRequest) {

        return openAiService.getMessage(type,readyRequest);
    }



    private static String defaultText() {
        return "(Возможно вы забыли поставить * перед названием)\nПример: *Типы классов";
    }

    private static String startText() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Задачник по java\n");
        stringBuilder.append("Что бы получить задачу просто напиши * далее тему").append("\n");
        stringBuilder.append("Пример: *Массивы");
        return stringBuilder.toString();
    }
}
