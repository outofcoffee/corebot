FROM openjdk:8-jdk

MAINTAINER Pete Cornish <outofcoffee@gmail.com>

RUN mkdir -p /opt/corebot /opt/corebot/config

ADD build/install/bots-websocket-items /opt/corebot

RUN ln -s /opt/corebot/bin/bots-websocket-items /usr/local/bin/corebot && \
    chmod +x /usr/local/bin/corebot

EXPOSE 8025

WORKDIR /opt/corebot

ENTRYPOINT [ "corebot" ]
