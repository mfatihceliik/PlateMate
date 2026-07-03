-- V23 added icon_url/background_color_hex/icon_tint_color_hex but never populated icon_url,
-- so every platform's icon fell back to the generic "link" icon on the client.
-- Simple Icons (MIT-licensed, https://simpleicons.org) served via jsDelivr is a stable public
-- CDN of brand SVGs; the client tints the SVG via Icon(tint=...), so the baked-in fill color
-- in these files does not matter.
UPDATE public.social_platforms SET icon_url = 'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/instagram.svg' WHERE code = 'INSTAGRAM';
UPDATE public.social_platforms SET icon_url = 'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/x.svg' WHERE code = 'X';
UPDATE public.social_platforms SET icon_url = 'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/facebook.svg' WHERE code = 'FACEBOOK';
UPDATE public.social_platforms SET icon_url = 'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/linkedin.svg' WHERE code = 'LINKEDIN';
UPDATE public.social_platforms SET icon_url = 'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/github.svg' WHERE code = 'GITHUB';
UPDATE public.social_platforms SET icon_url = 'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/snapchat.svg' WHERE code = 'SNAPCHAT';
