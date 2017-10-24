FROM openjdk:8-jdk

MAINTAINER Pete Cornish <outofcoffee@gmail.com>

RUN mkdir -p /opt/corebot /opt/corebot/config

ADD build/install/bots-http-slack /opt/corebot

RUN ln -s /opt/corebot/bin/bots-http-slack /usr/local/bin/corebot && \
    chmod +x /usr/local/bin/corebot

EXPOSE 8080

WORKDIR /opt/corebot

ENTRYPOINT [ "corebot" ]
