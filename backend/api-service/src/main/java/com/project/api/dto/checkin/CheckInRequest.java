package com.project.api.dto.checkin;

public record CheckInRequest(String message, Integer moodScore) {

    public CheckInRequest() {
        this(null, null);
    }

    public CheckInRequest(String message) {
        this(message, null);
    }
}
