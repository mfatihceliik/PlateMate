-- Align WIP catalog columns to the entity mappings (varchar, not fixed-width char).
-- V27/V28 declared these CHAR(n); the JPA entities map varchar(n) -> ddl-auto=validate fails.
ALTER TABLE public.premium_plans ALTER COLUMN currency   TYPE VARCHAR(3);
ALTER TABLE public.accent_colors ALTER COLUMN hex        TYPE VARCHAR(7);
ALTER TABLE public.user_settings ALTER COLUMN accent_hex TYPE VARCHAR(7);
