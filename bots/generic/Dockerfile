FROM openjdk:8-jdk

MAINTAINER Pete Cornish <outofcoffee@gmail.com>

RUN mkdir -p /opt/corebot /opt/corebot/config /opt/corebot/plugins

ADD build/install/bots-generic /opt/corebot

RUN ln -s /opt/corebot/bin/bots-generic /usr/local/bin/corebot && \
    chmod +x /usr/local/bin/corebot

WORKDIR /opt/corebot

ENTRYPOINT [ "corebot" ]
CMD [ "run" ]
