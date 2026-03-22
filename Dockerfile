FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY houseping-app/build/libs/houseping-app-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 10030

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-Dspring.profiles.active=${SPRING_PROFILE:-local}", "-jar", "app.jar"]
