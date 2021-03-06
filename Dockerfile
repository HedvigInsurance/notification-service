
##### Dependencies stage #####
FROM maven:3.6.3-amazoncorretto-11 AS dependencies
WORKDIR /usr/app

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

ENV MAVEN_OPTS="-Dmaven.repo.local=/usr/share/maven/ref/repository -DGITHUB_USERNAME=$GITHUB_USERNAME -DGITHUB_TOKEN=$GITHUB_TOKEN"

# Resolve dependencies and cache them
COPY pom.xml .
COPY settings.xml .
RUN mvn dependency:go-offline -s settings.xml


##### Build stage #####
FROM dependencies AS build

# Copy application source and build it
COPY src/main src/main
COPY lombok.config .
RUN mvn clean package -s settings.xml


##### Test stage #####
FROM build AS test
# Nothing to do - this service only has integration tests


##### Integration test stage #####
FROM build AS integration_test
# Copy test source and build+run tests
COPY src/test src/test
RUN mvn test-compile -s settings.xml
ENV TEST_DB_URL=jdbc:postgresql://test_db:5432
ENTRYPOINT ["mvn", "integration-test", "-f", "/usr/app/pom.xml", "-s", "/usr/share/maven/ref/settings-docker.xml"]


##### Assemble stage #####
FROM amazoncorretto:11 AS assemble

# Fetch the datadog agent
RUN curl -o dd-java-agent.jar -L 'https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.datadoghq&a=dd-java-agent&v=LATEST'

# Copy the jar from build stage to this one
COPY --from=build /usr/app/target/notification-service-0.0.1-SNAPSHOT.jar .

# Define entry point
ENTRYPOINT java -javaagent:/dd-java-agent.jar -jar notification-service-0.0.1-SNAPSHOT.jar
