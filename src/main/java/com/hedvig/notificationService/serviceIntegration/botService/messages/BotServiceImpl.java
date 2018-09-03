package com.hedvig.notificationService.serviceIntegration.botService.messages;

import lombok.val;
import org.springframework.stereotype.Service;

@Service
public class BotServiceImpl implements BotService {

  final private BotServiceClient botServiceClient;

    public BotServiceImpl(BotServiceClient botServiceClient) {
        this.botServiceClient = botServiceClient;
    }


    @Override
	public String pushTokenId(String hid, String token) {
        val pushTokenDto = botServiceClient.getPushTokenByHid(hid, token);
        return pushTokenDto.getToken();
	}
}
