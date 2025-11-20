FROM eclipse-temurin:21
COPY . /app
WORKDIR /app
RUN javac Hello.java
CMD ["java", "Hello"]
