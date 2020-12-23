#FROM gradle:6.4.0-jdk8 as cache
#LABEL maintainer yuanhongbin9090@gmail.com
#
#RUN mkdir -p /home/gradle/cache_home
#
#ENV GRADLE_USER_HOME /home/gradle/cache_home
#
#WORKDIR /code
#COPY build.gradle build.gradle
#
#RUN gradle clean build -i --stacktrace
#
#
#FROM gradle:6.4.0-jdk8 as builder
#LABEL maintainer yuanhongbin9090@gmail.com
#
## copy cache
#COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
#
## copy source code
#WORKDIR /code
#COPY build.gradle build.gradle
#COPY src src
#
## build
#RUN gradle build -x test -i --stacktrace
#
#


#FROM openjdk:8-jdk-alpine as prod
FROM ubuntu:18.04 as prod

#RUN apk --no-cache add bash curl wget
RUN apt-get update \
    && apt-get -y install openjdk-8-jre \
    && rm -rf /var/lib/apt/lists/*

#COPY --from=builder /code/dist/lib                  /dist/lib
#COPY --from=builder /code/dist/conf_template        /dist/conf
#COPY --from=builder /code/dist/apps                 /dist/apps
COPY lib                  /dist/lib
COPY conf_template        /dist/conf
COPY apps                 /dist/apps

WORKDIR /dist
EXPOSE 5002

ENV CLASSPATH "/dist/conf/:/dist/apps/*:/dist/lib/*"

ENV JAVA_OPTS " -server -Dfile.encoding=UTF-8 -Xmx256m -Xms256m -Xmn128m -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/log/heap_error.log  -XX:+UseG1GC -XX:MaxGCPauseMillis=200 "
ENV APP_MAIN "com.webank.webase.front.Application"

# start commond
ENTRYPOINT java ${JAVA_OPTS} -Djdk.tls.namedGroups="secp256k1", -Duser.timezone="Asia/Shanghai" -Djava.security.egd=file:/dev/./urandom, -Djava.library.path=/dist/conf -cp ${CLASSPATH}  ${APP_MAIN}
