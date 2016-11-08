FROM java:8-jdk

MAINTAINER Pete Cornish <outofcoffee@gmail.com>

RUN mkdir -p /opt/corebot /opt/corebot/config

ADD bot/build/install/bot /opt/corebot

WORKDIR "/opt/corebot"

ENTRYPOINT [ "./bin/bot" ]
