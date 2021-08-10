FROM ubuntu:18.04 as prod

#RUN apk --no-cache add bash curl wget
RUN apt-get update \
    && apt-get -y install openjdk-8-jre \
    && rm -rf /var/lib/apt/lists/*

COPY gradle                /dist/gradle
COPY static                /dist/static
COPY lib                  /dist/lib
COPY conf_template        /dist/conf
COPY apps                 /dist/apps

# run with 'sdk' volume
RUN mkdir -p /dist/sdk

WORKDIR /dist
EXPOSE 5002

ENV CLASSPATH "/dist/conf/:/dist/apps/*:/dist/lib/*"

ENV JAVA_OPTS " -server -Dfile.encoding=UTF-8 -Xmx512m -Xms512m -Xmn256m -Xss512k -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/log/heap_error.log  -XX:+UseG1GC -XX:MaxGCPauseMillis=200 "
ENV APP_MAIN "com.webank.webase.front.Application"

# start commond
ENTRYPOINT cp -r /dist/sdk/* /dist/conf/ && java ${JAVA_OPTS} -Djdk.tls.namedGroups="secp256k1", -Duser.timezone="Asia/Shanghai" -Djava.security.egd=file:/dev/./urandom, -Djava.library.path=/dist/conf -cp ${CLASSPATH}  ${APP_MAIN}
