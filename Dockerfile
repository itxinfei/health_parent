# 多阶段构建：Maven 构建 + JDK 25 运行
# Maven 工具从官方 maven:3.9 镜像拷贝（maven 官方镜像尚无 temurin-25 组合标签）

FROM maven:3.9 AS maven-tools

FROM eclipse-temurin:25-jdk AS build
COPY --from=maven-tools /usr/share/maven /usr/share/maven
ENV PATH="/usr/share/maven/bin:${PATH}"
WORKDIR /build
COPY pom.xml ./
COPY xinfei-common ./xinfei-common
COPY xinfei-modules ./xinfei-modules
COPY xinfei-admin ./xinfei-admin
RUN mvn -q -B clean package -DskipTests -pl xinfei-admin -am

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/xinfei-admin/target/*.jar app.jar
ENV TZ=Asia/Shanghai JAVA_OPTS=""
EXPOSE 8180
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
