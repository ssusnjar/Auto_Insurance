FROM maven:3.9.6-eclipse-temurin-21

WORKDIR /app
COPY . .

RUN mvn clean install

EXPOSE 8080
CMD ["mvn", "spring-boot:run"]