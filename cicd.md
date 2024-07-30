Sure, here is a comprehensive guide on setting up a complete CI/CD pipeline using GitLab to build a Java Maven Spring Boot application, containerize it with Docker, and deploy it to a Nexus repository. This guide includes all necessary steps from setting up the Nexus repository to configuring the GitLab pipeline.

### Prerequisites

1. **Nexus Repository Manager**: Ensure Nexus Repository Manager is installed and running.
2. **GitLab Repository**: Ensure you have a GitLab repository for your project.
3. **Docker**: Ensure Docker is installed and running on your local machine.

### Steps

1. **Set Up Nexus Repository Manager**
2. **Create a Spring Boot Application**
3. **Dockerize the Spring Boot Application**
4. **Configure GitLab CI/CD Pipeline**

### 1. Set Up Nexus Repository Manager

#### Install and Run Nexus

Download and install Nexus Repository Manager from the official [Sonatype website](https://www.sonatype.com/nexus-repository-oss).

#### Create a Docker (hosted) Repository

1. **Log in to Nexus**: Access Nexus Repository Manager at `http://<nexus-host>:8081`.
2. **Create a Docker Repository**:
   - Click on the **Gear** icon (Repository Administration).
   - Select **Repositories**.
   - Click on the **Create repository** button.
   - Choose **docker (hosted)**.
   - Configure the repository:
     - **Name**: `docker-repo`
     - **HTTP**: Enable and set a port (e.g., 8082)
     - **Docker API version**: `V2`
     - **Deployment Policy**: `Allow redeploy`
   - Save the repository configuration.

### 2. Create a Spring Boot Application

Create a new Spring Boot application or use an existing one. Ensure your `pom.xml` is correctly configured.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>demo</name>
    <description>Demo project for Spring Boot</description>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.4</version>
        <relativePath/>
    </parent>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3. Dockerize the Spring Boot Application

Create a `Dockerfile` in the root directory of your project:

```Dockerfile
# Use an official Maven image to build the app
FROM maven:3.8.1-jdk-11 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim
# Set the working directory in the container
WORKDIR /app
# Copy the jar file from the previous stage
COPY --from=build /home/app/target/demo-0.0.1-SNAPSHOT.jar /app/demo.jar
# Run the jar file
ENTRYPOINT ["java","-jar","/app/demo.jar"]
```

### 4. Configure GitLab CI/CD Pipeline

Create a `.gitlab-ci.yml` file in the root directory of your project:

```yaml
image: docker:latest

services:
  - docker:dind

variables:
  DOCKER_HOST: tcp://docker:2375
  DOCKER_DRIVER: overlay2

stages:
  - build
  - dockerize
  - deploy

before_script:
  - docker login nexus.example.com:8082 -u $NEXUS_USERNAME -p $NEXUS_PASSWORD

build:
  stage: build
  image: maven:3.8.1-jdk-11
  script:
    - mvn clean package
  artifacts:
    paths:
      - target/*.jar

dockerize:
  stage: dockerize
  script:
    - docker build -t nexus.example.com:8082/docker-repo/demo:latest .
    - docker push nexus.example.com:8082/docker-repo/demo:latest
  only:
    - main

deploy:
  stage: deploy
  script:
    - echo "Deploying to Nexus"
  only:
    - main
```

### Explanation

1. **Define the Image and Services**:
   - Use Docker as the base image and Docker-in-Docker service.
2. **Define Variables**:
   - `DOCKER_HOST` and `DOCKER_DRIVER` are set up for Docker-in-Docker.
3. **Stages**:
   - Define the stages: `build`, `dockerize`, and `deploy`.
4. **before_script**:
   - Log in to the Nexus Docker repository using environment variables `NEXUS_USERNAME` and `NEXUS_PASSWORD`.
5. **Build Stage**:
   - Use Maven to build the application and save the jar file as an artifact.
6. **Dockerize Stage**:
   - Build the Docker image and push it to the Nexus repository.
7. **Deploy Stage**:
   - Placeholder stage for additional deployment steps.

### Set Environment Variables in GitLab

1. **Go to your GitLab project**.
2. **Settings > CI/CD > Variables**:
   - Add `NEXUS_USERNAME` and `NEXUS_PASSWORD` with your Nexus credentials.

### Pulling Docker Images from Nexus

To pull an image from your Nexus repository, ensure you're logged in and use the pull command:

```sh
docker login nexus.example.com:8082
docker pull nexus.example.com:8082/docker-repo/demo:latest
```

By following these steps, you can set up a complete CI/CD pipeline using GitLab to build, containerize, and deploy a Java Maven Spring Boot application to a Nexus repository.
