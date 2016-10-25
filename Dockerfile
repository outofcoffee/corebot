FROM java:8-jdk

MAINTAINER Pete Cornish <outofcoffee@gmail.com>

RUN mkdir -p /opt/rundeck-slackbot /opt/rundeck-slackbot/config

ADD build/install/rundeck-slackbot /opt/rundeck-slackbot

WORKDIR "/opt/rundeck-slackbot"

ENTRYPOINT [ "./bin/rundeck-slackbot" ]
