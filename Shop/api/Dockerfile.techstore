FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app

# Crear un usuario de sistema sin privilegios por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR generado localmente desde la carpeta target
COPY target/ROOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]