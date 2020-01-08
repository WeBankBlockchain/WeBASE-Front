FROM fiscoorg/fiscobcos:v2.2.0

LABEL maintainer service@fisco.com.cn

WORKDIR /data

ADD jdk-8u211-linux-x64.tar.gz /usr/local/

ENV JAVA_HOME /usr/local/jdk1.8.0_211
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
ENV PATH $PATH:$JAVA_HOME/bin

ADD dist /dist

ENTRYPOINT ["sh","/dist/start.sh"]


EXPOSE 30300 20200 8545 5002
