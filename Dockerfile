FROM amazoncorretto:11

RUN curl -o dd-java-agent.jar -L 'https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.datadoghq&a=dd-java-agent&v=LATEST'

ADD target/notification-service-0.0.1-SNAPSHOT.jar /

ENTRYPOINT java -javaagent:/dd-java-agent.jar -jar notification-service-0.0.1-SNAPSHOT.jar
