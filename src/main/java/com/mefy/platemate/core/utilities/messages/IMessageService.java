package com.mefy.platemate.core.utilities.messages;

public interface IMessageService {
    String getMessage(String key);
    String getMessage(String key, Object... args);
}
