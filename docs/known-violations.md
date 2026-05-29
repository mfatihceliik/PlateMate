---
type: backend-doc
area: known-violations
updated: 2026-05-29
---

# Known Violations

Dağınık Open Questions ve mimari ihlal notlarının tek bir merkezi dosyada toplanmış halidir.
Refactor, architecture veya bug fix tasklarında bu dosya kontrol edilmeli.
Yeni violation eklenirse bu dosya güncellenmeli.
Çözülen violation status olarak güncellenmeli.

| ID | Violation | File/Area | Severity | Status | Suggested Fix |
|---|---|---|---|---|---|
| V01 | Business layer’ın API/socket layer’a bağımlı olması | NotificationManager | High | Resolved | Bağımlılık ISocketPushService interface'i arkasına alınarak çözüldü. |
| V02 | SocketIOServer doğrudan kullanımı | NotificationManager | High | Resolved | ISocketPushService interface'i ve api.socket altındaki somut implementasyonu ile doğrudan kullanım engellendi. |
| V03 | api.socket.utilities.constants.SocketEvents import edilmesi | NotificationManager | High | Resolved | SocketEvents sınıfı business.utilities.constants altına taşınarak çözüldü. |
| V04 | IParticipantDao doğrudan inject edilmesi (API katmanından Repository'e) | ChatSocketHandler | High | Resolved | IParticipantService üzerinden isRoomMember metodu kullanılarak çözüldü. |
| V05 | Socket error response olarak raw exception message dönülmesi | ChatSocketHandler | High | Open | Raw exception mesajlarını gizle, istemciye generic hata mesajı dön, sunucu tarafında logla. |
| V06 | Service interface içinde nested exception class bulunması | IRefreshTokenService | Medium | Resolved | İç içe sınıf business.exceptions dizinine taşınarak çözüldü. |
| V07 | Entity parametreli overload bulunması | IChatMessageService | Medium | Resolved | Kullanılmayan `sendMessage(ChatMessage, Long)` metodu interface ve manager sınıfından silindi. |
| V08 | Metotların entity return etmesi | ICityService | Medium | Resolved | CityDto ve CityMapper oluşturularak metot dönüş tipleri DTO'ya çevrildi. |
| V09 | Service interface'lerinin Entity import etmesi | INotificationService, IParticipantService, IPlateReportService | Low | Open | Parametreleri id veya string koda çevir. |
| V10 | Missing `@Transactional` on write operations | ParticipantManager, SocialMediaLinkManager, UserManager, UserSettingsManager | Medium | Resolved | Belirtilen Manager'lardaki public yazma (create/update/delete) metotlarına `@Transactional` annotation'ı eklendi. |
| V11 | Mixed message key pattern (string literal keys ve Messages constants bir arada) | AuthManager, ModerationAdminManager vb. | Medium | Partial | AuthManager, ModerationAdminManager, PlateRemovalRequestManager ve CommentReportManager sınıflarındaki literal string'ler Constants pattern'ine taşındı. Diğer sınıflarda kalan kullanımlar devam ediyor. |
| V12 | Turkish comments remaining in source code | Çeşitli dosyalar | Low | Partial | Yorumları İngilizceye çevir veya gereksizse sil. |
| V13 | Excessive dependency count | PlateManager | Low | Open | Sorumlulukları böl (PlateSearchManager, PlateReviewManager, PlateModerationManager, PlateDiscoveryManager). Yeni feature eklerken `services-business.md` içindeki **PlateManager Refactor Plan** bölümüne uy. |
| V14 | `/api/cities/**` exclude var ama city controller yok | WebMvcConfig | Low | Open | CityController eklenmeli (Endpoint API docs'ta ve Business katmanında mevcut). Exclude kuralı gereksiz değil, henüz tamamlanmamış bir feature. |
| V15 | Controller interface pattern'ini takip etmemesi | FcmTokensController | Low | Resolved | IFcmTokensController interface'i oluşturuldu ve FcmTokensController tarafından implement edildi. |
| V16 | Raw exception leak in 500 & TypeMismatch errors | GlobalExceptionHandler | High | Resolved | IMessageService ve server-side logging kullanılarak çözüldü. |
