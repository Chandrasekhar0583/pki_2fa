###############################
# Stage 1 — Builder
###############################
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests


###############################
# Stage 2 — Runtime
###############################
FROM eclipse-temurin:17-jre-jammy

ENV TZ=UTC
WORKDIR /app

RUN apt-get update && \
    apt-get install -y cron python3 tzdata curl && \
    ln -snf /usr/share/zoneinfo/UTC /etc/localtime && \
    echo "UTC" > /etc/timezone && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Create required directories
RUN mkdir -p /data /cron /scripts /app/keys && chmod 755 /data /cron /scripts

# Copy Spring Boot JAR
COPY --from=builder /app/target/*.jar app.jar

# ✅ COPY RSA KEYS FROM data/
COPY data/student_private.pem /app/keys/student_private.pem
COPY data/student_public.pem /app/keys/student_public.pem
COPY data/instructor_public.pem /app/keys/instructor_public.pem

# Copy scripts
COPY scripts /scripts
RUN chmod +x /scripts/log_2fa_cron.py

# Copy cron config
COPY cron/2fa-cron /cron/2fa-cron
RUN chmod 644 /cron/2fa-cron && crontab /cron/2fa-cron

# Prepare cron output file
RUN touch /cron/last_code.txt && chmod 666 /cron/last_code.txt

EXPOSE 8080

CMD cron && java -jar app.jar
