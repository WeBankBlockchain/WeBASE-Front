FROM java:8


COPY lib                  /dist/lib
COPY conf                 /dist/conf
COPY apps                 /dist/apps


WORKDIR /dist
EXPOSE 5003

ENV CLASSPATH "/dist/conf/:/dist/apps/*:/dist/lib/*"

ENV JAVA_OPTS " -server -Dfile.encoding=UTF-8 -Xmx512m -Xms512m -Xmn256m -Xss512k -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/log/heap_error.log  -XX:+UseG1GC -XX:MaxGCPauseMillis=200 "
ENV APP_MAIN "com.webank.webase.transaction.Application"

ENTRYPOINT   java ${JAVA_OPTS} -Djdk.tls.namedGroups="secp256k1", -Duser.timezone="Asia/Shanghai" -Djava.security.egd=file:/dev/./urandom, -Djava.library.path=/dist/conf -cp ${CLASSPATH}  ${APP_MAIN}