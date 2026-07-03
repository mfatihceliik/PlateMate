package com.mefy.platemate.config;

import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * netty-socketio kendi {@link com.fasterxml.jackson.databind.ObjectMapper}'ını kullanır; Spring'in
 * REST mapper'ından bağımsızdır ve varsayılan olarak Java 8 tarih/saat tiplerini bilmez. Bu yüzden
 * {@code LocalDateTime} alanı taşıyan bir DTO (ör. {@code ChatMessageDto.sentAt}) socket event'i
 * olarak gönderilirken serileştirme hata verir ve paket sessizce düşer — event hiç ulaşmaz.
 *
 * <p>{@link JavaTimeModule} kaydederek tarihleri serileştirilebilir kılar ve REST ile tutarlı olması
 * için timestamp (dizi) yerine ISO-8601 string yazdırır.
 */
public class SocketJacksonJsonSupport extends JacksonJsonSupport {

    public SocketJacksonJsonSupport() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
