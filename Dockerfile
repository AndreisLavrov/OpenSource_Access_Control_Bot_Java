FROM openjdk:21-jdk-slim

WORKDIR /app

COPY Accesscontrolbot/target/Access-control-bot-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
