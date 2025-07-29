FROM maven:3.9.6-eclipse-temurin-21

WORKDIR /app
COPY . .

EXPOSE 8080
CMD ["mvn", "clean", "spring-boot:run"]