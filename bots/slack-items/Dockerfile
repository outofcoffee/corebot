FROM eclipse-temurin:8u322-b06-jre-focal

MAINTAINER Pete Cornish <outofcoffee@gmail.com>

RUN mkdir -p /opt/corebot /opt/corebot/config

ADD build/install/bots-slack-items /opt/corebot

RUN ln -s /opt/corebot/bin/bots-slack-items /usr/local/bin/corebot && \
    chmod +x /usr/local/bin/corebot

WORKDIR /opt/corebot

ENTRYPOINT [ "corebot" ]
