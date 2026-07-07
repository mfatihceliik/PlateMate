package com.mefy.platemate.business.utilities.plate.concrete;

import com.mefy.platemate.business.utilities.plate.abstracts.IPlateValidator;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TrPlateValidator implements IPlateValidator {
    // Regex Description:
    // ^(0[1-9]|[1-7][0-9]|8[0-1]) -> City codes from 01 to 81
    // (([A-Z])(\d{4,5})           -> 99 X 9999 or 99 X 99999
    // |([A-Z]{2})(\d{3,4})        -> 99 XX 999 or 99 XX 9999
    // |([A-Z]{3})(\d{2,3}))$      -> 99 XXX 99 or 99 XXX 999
    private static final String TR_PLATE_REGEX = "^(0[1-9]|[1-7][0-9]|8[0-1])(([A-Z])(\\d{4,5})|([A-Z]{2})(\\d{3,4})|([A-Z]{3})(\\d{2,3}))$";

    @Override
    public boolean isValid(String plateCode) {
        if (plateCode == null) return false;

        // Remove spaces and convert to uppercase (Normalization)
        String normalizedPlate = plateCode.replace(" ", "").toUpperCase(Locale.ROOT);

        return normalizedPlate.matches(TR_PLATE_REGEX);
    }
}
