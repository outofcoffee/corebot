FROM openjdk:8-jdk

MAINTAINER Pete Cornish <outofcoffee@gmail.com>

RUN mkdir -p /opt/corebot /opt/corebot/config

ADD bots/slack-deploy/build/install/slack-deploy /opt/corebot

WORKDIR "/opt/corebot"

ENTRYPOINT [ "./bin/slack-deploy" ]
