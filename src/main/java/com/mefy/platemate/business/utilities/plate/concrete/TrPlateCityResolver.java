package com.mefy.platemate.business.utilities.plate.concrete;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class TrPlateCityResolver {
    private static final Map<Integer, String> CITY_NAMES = Map.ofEntries(
            Map.entry(1, "Adana"),
            Map.entry(2, "Adiyaman"),
            Map.entry(3, "Afyonkarahisar"),
            Map.entry(4, "Agri"),
            Map.entry(5, "Amasya"),
            Map.entry(6, "Ankara"),
            Map.entry(7, "Antalya"),
            Map.entry(8, "Artvin"),
            Map.entry(9, "Aydin"),
            Map.entry(10, "Balikesir"),
            Map.entry(11, "Bilecik"),
            Map.entry(12, "Bingol"),
            Map.entry(13, "Bitlis"),
            Map.entry(14, "Bolu"),
            Map.entry(15, "Burdur"),
            Map.entry(16, "Bursa"),
            Map.entry(17, "Canakkale"),
            Map.entry(18, "Cankiri"),
            Map.entry(19, "Corum"),
            Map.entry(20, "Denizli"),
            Map.entry(21, "Diyarbakir"),
            Map.entry(22, "Edirne"),
            Map.entry(23, "Elazig"),
            Map.entry(24, "Erzincan"),
            Map.entry(25, "Erzurum"),
            Map.entry(26, "Eskisehir"),
            Map.entry(27, "Gaziantep"),
            Map.entry(28, "Giresun"),
            Map.entry(29, "Gumushane"),
            Map.entry(30, "Hakkari"),
            Map.entry(31, "Hatay"),
            Map.entry(32, "Isparta"),
            Map.entry(33, "Mersin"),
            Map.entry(34, "Istanbul"),
            Map.entry(35, "Izmir"),
            Map.entry(36, "Kars"),
            Map.entry(37, "Kastamonu"),
            Map.entry(38, "Kayseri"),
            Map.entry(39, "Kirklareli"),
            Map.entry(40, "Kirsehir"),
            Map.entry(41, "Kocaeli"),
            Map.entry(42, "Konya"),
            Map.entry(43, "Kutahya"),
            Map.entry(44, "Malatya"),
            Map.entry(45, "Manisa"),
            Map.entry(46, "Kahramanmaras"),
            Map.entry(47, "Mardin"),
            Map.entry(48, "Mugla"),
            Map.entry(49, "Mus"),
            Map.entry(50, "Nevsehir"),
            Map.entry(51, "Nigde"),
            Map.entry(52, "Ordu"),
            Map.entry(53, "Rize"),
            Map.entry(54, "Sakarya"),
            Map.entry(55, "Samsun"),
            Map.entry(56, "Siirt"),
            Map.entry(57, "Sinop"),
            Map.entry(58, "Sivas"),
            Map.entry(59, "Tekirdag"),
            Map.entry(60, "Tokat"),
            Map.entry(61, "Trabzon"),
            Map.entry(62, "Tunceli"),
            Map.entry(63, "Sanliurfa"),
            Map.entry(64, "Usak"),
            Map.entry(65, "Van"),
            Map.entry(66, "Yozgat"),
            Map.entry(67, "Zonguldak"),
            Map.entry(68, "Aksaray"),
            Map.entry(69, "Bayburt"),
            Map.entry(70, "Karaman"),
            Map.entry(71, "Kirikkale"),
            Map.entry(72, "Batman"),
            Map.entry(73, "Sirnak"),
            Map.entry(74, "Bartin"),
            Map.entry(75, "Ardahan"),
            Map.entry(76, "Igdir"),
            Map.entry(77, "Yalova"),
            Map.entry(78, "Karabuk"),
            Map.entry(79, "Kilis"),
            Map.entry(80, "Osmaniye"),
            Map.entry(81, "Duzce")
    );

    public Optional<Integer> resolveCityId(String normalizedPlate) {
        if (normalizedPlate == null || normalizedPlate.length() < 2) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(normalizedPlate.substring(0, 2)));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    public Optional<String> resolveCityName(String normalizedPlate) {
        return resolveCityId(normalizedPlate).map(CITY_NAMES::get);
    }
}
