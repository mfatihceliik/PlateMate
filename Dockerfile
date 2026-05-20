# Aşama 1: Build (Uygulamayı derleme aşaması)
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Tüm dosyaları kopyala
COPY . .

# Gradlew dosyasının çalıştırılabilir olduğundan emin ol
RUN chmod +x ./gradlew

# Windows üzerinde oluşturulan dosyalardaki satır sonu (\r\n) sorunlarını düzeltmek için
RUN sed -i 's/\r$//' ./gradlew

# Projeyi derle (Testleri atlayarak daha hızlı derlenmesini sağlıyoruz)
RUN ./gradlew clean build -x test --no-daemon

# Aşama 2: Run (Sadece derlenmiş uygulamayı çalıştırma aşaması)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Spring Boot ve Socket.io portlarını dışarıya aç
EXPOSE 8080 9092

# İlk aşamadan derlenmiş jar dosyasını kopyala
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar

# Uygulamayı başlat
ENTRYPOINT ["java", "-jar", "app.jar"]
