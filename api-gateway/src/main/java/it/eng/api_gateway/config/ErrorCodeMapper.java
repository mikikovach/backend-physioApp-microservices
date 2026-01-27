package it.eng.api_gateway.config;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ErrorCodeMapper {

    private static final Map<String, String> MESSAGES = Map.of(
            "SLOT_BOOKED", "Slot je već rezervisan",
            "SLOT_NOT_FOUND", "Slot ne postoji",
            "SLOT_IN_PAST", "Slot je u prošlosti"
    );

    public String resolveMessage(String errorCode) {
        return MESSAGES.getOrDefault(errorCode, "Neočekivana greška");
    }
}
