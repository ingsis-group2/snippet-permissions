ARG DB_USER
ARG DB_PASSWORD
ARG DB_PORT

FROM gradle:8.7.0-jdk17

ENV DB_USER=${DB_USER}
ENV DB_PASSWORD=${DB_PASSWORD}
ENV DB_PORT=${DB_PORT}

COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build
EXPOSE 8080
ENTRYPOINT ["java","-jar","/home/gradle/src/build/libs/snippet-perms-0.0.1-SNAPSHOT.jar"]
