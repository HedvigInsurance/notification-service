package com.hedvig.notificationService.serviceIntegration.botService.messages.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import lombok.Getter;



@Getter
public class BotMessage {

    private JsonNode message;

    private Instant timestamp;

    private Long globalId;

    private Long messageId;

    private Long fromId;

    private JsonNode body;

    private JsonNode header;

    private String type;

    private String id;

    private String hid;



}
