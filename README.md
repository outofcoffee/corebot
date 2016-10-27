# Rundeck Slack bot

## Usage

Example:

    @rundeckbot deploy user-service 1.0 to staging
    
    > rundeckbot:
    > OK, I'm deploying user-service version 1.0 to staging.
    > Status of job is: running
    > Details: http://rundeck/jobs/abc/123

## Instructions

* As a Slack admin, create a Slack bot user and obtain its auth token 
* As a Rundeck admin, generate a Rundeck API token
* Set environment variables
* Run!

## Quick start

The quickest way to get up and running is to use the Docker image:

    docker run -d \
            --env SLACK_AUTH_TOKEN="CHANGEME" \
            --env SLACK_CHANNEL_NAME="rundeck-slackbot" \
            --env RUNDECK_API_TOKEN="CHANGEME" \
            --env RUNDECK_BASE_URL="http://rundeck:4440" \
            -v /path/to/config/dir:/opt/rundeck-slackbot/config \
            outofcoffee/rundeck-slackbot

Note: the container doesn't require any inbound ports to be exposed.

## Build

If instead you wish to build and run locally, you can run:

    ./gradlew installDist
    docker-compose build

Once built, set the following environment variables in `docker-compose.yml`:
    
    SLACK_AUTH_TOKEN: "CHANGEME"
    SLACK_CHANNEL_NAME: "rundeck-slackbot"
    RUNDECK_API_TOKEN: "CHANGEME"
    RUNDECK_BASE_URL: "http://rundeck:4440"

Then run with:

    docker-compose up

If you change anything, don't forget to rebuild before running again.

## More info

Slack API: https://api.slack.com/bot-users

Rundeck API: http://rundeck.org/2.6.9/api/index.html#running-a-job

### Rundeck

Any Rundeck instance can be used as long as it supports API v14 or higher.

As an example, here is an unofficial Rundeck Docker image: https://hub.docker.com/r/jordan/rundeck/

    docker run -it \
        -p 4440:4440 \
        -e SERVER_URL=http://localhost:4440 \
        jordan/rundeck

## Roadmap

* Lock/unlock command (with confirmation if unlocking another user's lock)
* Last deployment query (what version, who triggered it etc.)
