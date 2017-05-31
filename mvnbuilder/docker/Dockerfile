FROM java:8-jre
RUN mkdir /opt/turan
ADD builder.jar /opt/turan/
RUN chmod +x /opt/turan/builder.jar
EXPOSE 8080
EXPOSE 8000
ENTRYPOINT ["java", "-Xdebug","-agentlib:jdwp=transport=dt_socket,address=8000,suspend=n,server=y", "-jar", "/opt/turan/builder.jar" ]