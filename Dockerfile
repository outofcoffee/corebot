FROM openjdk:8-jdk

MAINTAINER Pete Cornish <outofcoffee@gmail.com>

RUN mkdir -p /opt/corebot /opt/corebot/config

ADD deploy/build/install/deploy /opt/corebot

WORKDIR "/opt/corebot"

ENTRYPOINT [ "./bin/deploy" ]
