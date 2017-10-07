FROM openjdk:8-jdk

MAINTAINER Pete Cornish <outofcoffee@gmail.com>

RUN mkdir -p /opt/corebot /opt/corebot/config

ADD build/install/bots-websocket-items /opt/corebot

EXPOSE 8025

WORKDIR "/opt/corebot"

ENTRYPOINT [ "./bin/bots-websocket-items" ]
