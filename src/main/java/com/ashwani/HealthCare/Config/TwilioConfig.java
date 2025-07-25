package com.ashwani.HealthCare.Config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class TwilioConfig {
    @Value("${TWILIO_ACCOUNT_SID}")
    private String accountSid;

    @Value("${TWILIO_API_KEY}")
    private String apiKey;

    @Value("${TWILIO_API_SECRET}")
    private String apiSecret;

    @Value("${TWILIO_AUTH_TOKEN}")
    private String authToken;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

}
