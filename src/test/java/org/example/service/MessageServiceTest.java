package org.example.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.ValueSources;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService();
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void getMessageResponse() {
    }


    @ParameterizedTest
    @ValueSource(strings =
            {"*массивы","**массивы","** массивы","*\nмассивы"})
    void editRequest(String value) throws Exception {
        String prefixTextMessage = "Напиши 1 задачу на java без решения.Тема задачи: ";

        //String result1 = messageService.editRequest(value);
       // Assertions.assertEquals(result1, prefixTextMessage + "массивы");

    }



}