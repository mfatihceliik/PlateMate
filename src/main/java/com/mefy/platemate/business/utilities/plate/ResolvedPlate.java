package com.mefy.platemate.business.utilities.plate;

import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.User;
import lombok.Getter;

@Getter
public class ResolvedPlate {
    private final User user;
    private final Plate plate;
    private final Result error;

    private ResolvedPlate(User user, Plate plate, Result error) {
        this.user = user;
        this.plate = plate;
        this.error = error;
    }

    public static ResolvedPlate ok(User user, Plate plate) {
        return new ResolvedPlate(user, plate, null);
    }

    public static ResolvedPlate error(Result error) {
        return new ResolvedPlate(null, null, error);
    }

    public boolean hasError() {
        return this.error != null;
    }

    public boolean isOk() { return error == null; }
}

