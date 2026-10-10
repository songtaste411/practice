# 镜像源可以改。国内机器如果拉不动，换成 docker.io 官方名，或换你自己的镜像仓库。
ARG MAVEN_IMAGE=swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/maven:3.9.9-eclipse-temurin-21
ARG JRE_IMAGE=swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/eclipse-temurin:21-jre

FROM ${MAVEN_IMAGE} AS build
WORKDIR /src
COPY maven-settings.xml /tmp/settings.xml
COPY pom.xml .
COPY src ./src
RUN mvn -B -s /tmp/settings.xml -DskipTests package

FROM ${JRE_IMAGE}
WORKDIR /app
COPY --from=build /src/target/prod-clinic-api.jar /app/prod-clinic-api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/prod-clinic-api.jar"]
