package com.mefy.platemate.business.utilities;

import java.util.regex.Pattern;

public class EmailValidator {

    // RFC 5322 standartlarına yakın, yaygın kullanılan bir Regex
    private static final String EMAIL_PATTERN =
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";

    public static boolean isEmailValid(String email) {
        if(email == null || email.isBlank())
            return false;
        Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(email).matches();
    }
}
