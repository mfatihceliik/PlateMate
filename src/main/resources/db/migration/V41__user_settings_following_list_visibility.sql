-- Başkalarının bu kullanıcının "takip ettiklerim" listesini görüp göremeyeceğini kontrol eder.
-- Takipçi listesi bundan etkilenmez, her zaman görünür. Varsayılan: görünür.
ALTER TABLE user_settings ADD COLUMN following_list_visible BOOLEAN NOT NULL DEFAULT TRUE;
