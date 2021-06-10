package com.paymybuddy.webapp.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateUtil {

    public LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now();
    }

}
