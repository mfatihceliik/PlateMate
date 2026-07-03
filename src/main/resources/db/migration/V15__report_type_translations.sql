CREATE TABLE plate_report_type_translations (
    id          BIGSERIAL    PRIMARY KEY,
    report_type_id BIGINT    NOT NULL REFERENCES plate_report_types(id) ON DELETE CASCADE,
    locale      VARCHAR(5)   NOT NULL,
    label       VARCHAR(120) NOT NULL,
    description VARCHAR(400),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE(report_type_id, locale)
);

CREATE INDEX idx_prt_translations_locale ON plate_report_type_translations(locale);

-- Seed English translations from existing data
INSERT INTO plate_report_type_translations (report_type_id, locale, label, description)
SELECT id, 'en', label, description
FROM plate_report_types;

-- Seed Turkish translations
INSERT INTO plate_report_type_translations (report_type_id, locale, label, description)
SELECT id, 'tr',
    CASE code
        WHEN 'POLITE'                 THEN 'Nazik'
        WHEN 'GAVE_WAY'              THEN 'Yol Verdi'
        WHEN 'RESPECTFUL'            THEN 'Saygılı'
        WHEN 'THANKED'               THEN 'Teşekkür Etti'
        WHEN 'PATIENT'               THEN 'Sabırlı'
        WHEN 'CAREFUL'               THEN 'Dikkatli'
        WHEN 'SAFE_DRIVING'          THEN 'Güvenli Sürüş'
        WHEN 'SPEEDING'              THEN 'Hız İhlali'
        WHEN 'AGGRESSIVE'            THEN 'Agresif Sürücü'
        WHEN 'TAILGATING'            THEN 'Takip Mesafesi İhlali'
        WHEN 'NO_SIGNAL'             THEN 'Sinyal Vermedi'
        WHEN 'HONKER'                THEN 'Kornacı'
        WHEN 'UNSAFE_LANE_CHANGE'    THEN 'Tehlikeli Şerit Değiştirme'
        WHEN 'SPEEDING_REPORT'       THEN 'Hız Raporu'
        WHEN 'PARKING_ISSUE'         THEN 'Park Sorunu'
        WHEN 'HONKING_DISTURBANCE'   THEN 'Korna Rahatsızlığı'
        WHEN 'TRAFFIC_RULE_VIOLATION' THEN 'Trafik Kuralı İhlali'
        WHEN 'POSITIVE_DRIVER'       THEN 'Pozitif Sürücü'
        WHEN 'HELPFUL_DRIVER'        THEN 'Yardımsever Sürücü'
        WHEN 'RESPECTFUL_DRIVER'     THEN 'Saygılı Sürücü'
        WHEN 'THANK_YOU_REPORT'      THEN 'Teşekkür Raporu'
        ELSE label
    END,
    CASE code
        WHEN 'POLITE'                 THEN 'Nazik ve düzgün davranış raporu.'
        WHEN 'GAVE_WAY'              THEN 'Yol verme davranışı raporu.'
        WHEN 'RESPECTFUL'            THEN 'Saygılı sürücü davranışı raporu.'
        WHEN 'THANKED'               THEN 'Teşekkür eden sürücü raporu.'
        WHEN 'PATIENT'               THEN 'Sabırlı sürücü davranışı raporu.'
        WHEN 'CAREFUL'               THEN 'Dikkatli sürüş davranışı raporu.'
        WHEN 'SAFE_DRIVING'          THEN 'Güvenli sürüş davranışı raporu.'
        WHEN 'SPEEDING'              THEN 'Hız sınırlarını aşan sürüş davranışı raporu.'
        WHEN 'AGGRESSIVE'            THEN 'Agresif sürüş davranışı raporu.'
        WHEN 'TAILGATING'            THEN 'Takip mesafesi ihlali raporu.'
        WHEN 'NO_SIGNAL'             THEN 'Sinyal vermeden manevraya başlama raporu.'
        WHEN 'HONKER'                THEN 'Aşırı korna kullanımı raporu.'
        WHEN 'UNSAFE_LANE_CHANGE'    THEN 'Tehlikeli veya ani şerit değiştirme davranışı raporu.'
        WHEN 'SPEEDING_REPORT'       THEN 'Potansiyel hız ihlali raporu.'
        WHEN 'PARKING_ISSUE'         THEN 'Trafik akışını etkileyen park davranışı raporu.'
        WHEN 'HONKING_DISTURBANCE'   THEN 'Aşırı korna rahatsızlığı raporu.'
        WHEN 'TRAFFIC_RULE_VIOLATION' THEN 'Trafik kuralı ihlali davranışı raporu.'
        WHEN 'POSITIVE_DRIVER'       THEN 'Pozitif ve düşünceli sürüş davranışı raporu.'
        WHEN 'HELPFUL_DRIVER'        THEN 'Trafikte yardımsever davranış raporu.'
        WHEN 'RESPECTFUL_DRIVER'     THEN 'Saygılı ve güvenli davranış raporu.'
        WHEN 'THANK_YOU_REPORT'      THEN 'Genel takdir raporu.'
        ELSE description
    END
FROM plate_report_types;
