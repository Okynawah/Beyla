FROM openjdk:17-slim

COPY src/query /code/beyla/query

WORKDIR /code/beyla/query/

RUN chmod +x mvnw

RUN ./mvnw clean package spring-boot:repackage

RUN mv target/*.jar /code/ && \
    rm -Rf /code/beyla /root/.m2 /root/.cache /tmp/* /var/tmp/*

WORKDIR /code

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SLEEP=10 \
    JAVA_OPTS=""

CMD echo "The application will start in ${SLEEP}s..." && \
    sleep ${SLEEP} && \
    java ${JAVA_OPTS} -jar /code/*.jar
    
EXPOSE 8080
