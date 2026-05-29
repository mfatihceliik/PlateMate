CREATE TABLE IF NOT EXISTS public.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    role_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    premium_until TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.plates (
    id BIGSERIAL PRIMARY KEY,
    plate_code VARCHAR(64) NOT NULL UNIQUE,
    city_id INTEGER,
    rating_average DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    review_count INTEGER NOT NULL DEFAULT 0,
    total_rating_sum BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS public.plate_reviews (
    id BIGSERIAL PRIMARY KEY,
    plate_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    comment VARCHAR(1000) NOT NULL DEFAULT '',
    status VARCHAR(32) NOT NULL DEFAULT 'APPROVED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_plate_reviews_plate
        FOREIGN KEY (plate_id) REFERENCES public.plates (id) ON DELETE CASCADE,
    CONSTRAINT fk_plate_reviews_user
        FOREIGN KEY (user_id) REFERENCES public.users (id) ON DELETE CASCADE
);
