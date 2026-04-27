package com.mefy.platemate.business.utilities.plate.concrete;

import com.mefy.platemate.business.utilities.plate.abstracts.IPlateValidator;
import org.springframework.stereotype.Component;

@Component
public class TrPlateValidator implements IPlateValidator {
    // Regex Description:
    // ^(0[1-9]|[1-7][0-9]|8[0-1]) -> 01'den 81'e kadar olan şehir kodları
    // (([A-Z])(\d{4,5})           -> 99 X 9999 veya 99 X 99999
    // |([A-Z]{2})(\d{3,4})        -> 99 XX 999 veya 99 XX 9999
    // |([A-Z]{3})(\d{2,3}))$      -> 99 XXX 99 veya 99 XXX 999
    private static final String TR_PLATE_REGEX = "^(0[1-9]|[1-7][0-9]|8[0-1])(([A-Z])(\\d{4,5})|([A-Z]{2})(\\d{3,4})|([A-Z]{3})(\\d{2,3}))$";

    @Override
    public boolean isValid(String plateCode) {
        if (plateCode == null) return false;

        // Boşlukları kaldır ve büyük harfe çevir (Normalizasyon)
        String normalizedPlate = plateCode.replace(" ", "").toUpperCase();

        return normalizedPlate.matches(TR_PLATE_REGEX);
    }
}
