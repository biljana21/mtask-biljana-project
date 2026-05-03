FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 9005

ENTRYPOINT ["java", "-jar", "app.jar"]