FROM adoptopenjdk/openjdk11:alpine-jre
COPY build/libs/zenith-0.0.1-SNAPSHOT.jar /opt/app/app.jar
EXPOSE 8080/tcp
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]