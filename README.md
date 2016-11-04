# Rundeck Slack bot

Trigger your [Rundeck](http://rundeck.org) jobs from [Slack](https://slack.com).

_Example:_

    @rundeckbot deploy user-service 1.0 to staging
    
    > rundeckbot:
    > OK, I'm deploying user-service version 1.0 to staging.
    > Status of job is: running
    > Details: http://rundeck/jobs/abc/123
    
> Why would you want this? Check out [ChatOps](http://blogs.atlassian.com/2016/01/what-is-chatops-adoption-guide/).

## What can it do?

##### Trigger your deployment jobs

<img alt="Deploy" src="https://github.com/outofcoffee/rundeck-slackbot/raw/master/docs/images/deploy.png" width="467">

##### Trigger your other custom jobs

<img alt="Restart" src="https://github.com/outofcoffee/rundeck-slackbot/raw/master/docs/images/restart.png" width="472">

##### Lock things to prevent accidental deployment

<img alt="Lock deployment failure" src="https://github.com/outofcoffee/rundeck-slackbot/raw/master/docs/images/lock_deploy_fail.png" width="371">

##### Unlock things you've locked

<img alt="Unlock job" src="https://github.com/outofcoffee/rundeck-slackbot/raw/master/docs/images/unlock.png" width="336">

##### Get help

<img alt="Help" src="https://github.com/outofcoffee/rundeck-slackbot/raw/master/docs/images/unknown.png" width="389">

## Instructions

* As a Slack admin, create a Slack bot user and obtain its access token - [instructions](https://my.slack.com/services/new/bot)
* As a Rundeck admin, generate a Rundeck API token - [instructions](http://rundeck.org/2.6.9/api/index.html#token-authentication)
* Set environment variables
* Run!

## Quick start

The quickest way to get up and running is to use the Docker image:

    docker run -d \
            --env SLACK_AUTH_TOKEN="CHANGEME" \
            --env SLACK_CHANNELS="rundeck-slackbot" \
            --env RUNDECK_API_TOKEN="CHANGEME" \
            --env RUNDECK_BASE_URL="http://rundeck:4440" \
            -v /path/to/actions.yml:/opt/rundeck-slackbot/actions.yml \
            outofcoffee/rundeck-slackbot

Note: the container doesn't require any inbound ports to be exposed.

## Build

If instead you wish to build and run locally, you can run:

    ./gradlew installDist
    docker-compose build

Once built, set the following environment variables in `docker-compose.yml`:
    
    SLACK_AUTH_TOKEN: "CHANGEME"
    SLACK_CHANNELS: "rundeck-slackbot"
    RUNDECK_API_TOKEN: "CHANGEME"
    RUNDECK_BASE_URL: "http://rundeck:4440"
    BOT_CONFIG: "/path/to/actions.yaml"
    
> Note: `SLACK_CHANNELS` is a comma-separated list of channel names.
> Note: the default path for `BOT_CONFIG` is `/opt/rundeck-slackbot/actions.yml`

Then run with:

    docker-compose up

If you change anything, don't forget to rebuild before running again.

## Actions and configuration

The bot has both built-in actions and custom actions. Examples of built in actions are the lock/unlock actions. Custom actions are triggers for your Rundeck jobs, configured using a configuration file, typically called `actions.yml`.

### Action configuration file

> Note: the default path for `BOT_CONFIG` is `/opt/rundeck-slackbot/actions.yml`

_Example:_
```
version: '1'
actions:
  services:
    jobId: 9374f1c8-7b3f-4145-8556-6b55551fb60f
    template: deploy services {version} to {environment}
```

File structure:

* All files must specify version ‘1’
* All actions must sit under a top level `actions` block
* Each action must have a name (it’s `services` in this example)
* Each action must have a Rundeck job ID (obtain this from Rundeck)
* Each action must have a template - more details below
* Each action may optionally specify a map of default options
* Each action may optionally specify a list of tags

#### Action template

An action template provides the syntax for invoking the command. 

_Example:_

	deploy services

A template also allows you to specify job options as placeholders, such as:

	deploy services {version} to {environment}

In this example both _version_ and _environment_ are captured from the command, such as:

	@rundeckbot deploy services 1.0 to UAT

This will result in the action being fired, passing the following options:

- version=1.0
- environment=UAT

#### Static options

You might want to pass an option value to a job every time, and not require the user to provide it. You can do this with the `static` section of the `options` action block:

```
version: '1'
actions:
  services:
    jobId: 9374f1c8-7b3f-4145-8556-6b55551fb60f
    template: deploy services {version} to {environment}
    options:
      static:
        myOption: someValue
        myOtherOption: someOtherValue
```

This will result in the action being fired, passing the following options:

- version=1.0
- environment=UAT
- _myOption=someValue_

#### Transforming options

You might want to transform an option value provided by a user before it is passed to a job. You can do this with the `transformers` section of the `options` action block:

```
version: '1'
actions:
  services:
    jobId: 9374f1c8-7b3f-4145-8556-6b55551fb60f
    template: deploy services {version} to {environment}
    options:
      transformers:
        version:
          - LOWERCASE
        environment:
          - UPPERCASE
```

If the user typed this command:

    @rundeckbot deploy services V1.0 to uat

This will result in the action being fired, passing the following options:

- version=v1.0 (note: lowercased)
- environment=UAT (note: uppercased)

#### Tags and multiple job actions

Sometimes actions can be run on multiple jobs. To do this, set the `tags` block:

```
version: '1'
actions:
  deploy-services:
    jobId: 9374f1c8-7b3f-4145-8556-6b55551fb60f
    template: deploy services {version} to {environment}
    tags:
	  - services
	    
  restart-services:
    jobId: e9d12eec-abff-4780-89cd-56a48b8c67be
    template: restart services in {environment}
    tags:
	  - services
```

Here, two actions are defined: `deploy-services` and `restart-services`, both tagged with `services`. This means you can do things like:

	@rundeckbot lock services

…and both actions will be locked.

> Tip: There is a special tag set on all actions, named 'all'. This means you can do things like `@rundeckbot lock all`.

#### Security

You can choose which users are authorised to perform actions, using the `security` block:

```
security:
  users:
    # alice uses the built-in 'admin' role
    alice:
      roles:
        - admin
```
> *Important:* if you do not specify a security configuration explicitly, the default will be used. The default settings permit all users to perform all actions.

There is a built in role, named `admin`, which you can assign to users. You can also define your own roles, listing the permissions granted to users with that role.

You can assign roles on a per-username basis or, if you wish to assign certain roles to all users, use the special `"*"` key, as shown in the example below:

```
security:
  roles:
    # a role that can only trigger jobs
    deployer:
      permissions:
        - trigger
      tags:
        - all

  users:
    # alice uses the built-in 'admin' role
    alice:
      roles:
        - admin

    # all users can trigger jobs
    "*":
      roles:
        - deployer
```

### Built-in actions

There are a number of built in actions, such as:

* `@rundeckbot help` - show usage information.
* `@rundeckbot lock {action name or tag}` - lock action(s) to prevent them being triggered accidentally.
* `@rundeckbot unlock {action name or tag}` - unlock locked action(s).
* `@rundeckbot status {action name or tag}` - show status of action(s).
* `@rundeckbot enable {action name or tag}` - set the Rundeck execution status for a job - *Note:* this requires the Rundeck ACL to permit the API user to set the execution status of a job.
* `@rundeckbot disable {action name or tag}` - set the Rundeck execution status for a job - *Note:* this requires the Rundeck ACL to permit the API user to set the execution status of a job.

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

* Notify lock owner on unlock by another user.
* Last deployment query (what version, who triggered it etc.).
* Status check should query Rundeck job status.
* Add stop/abort action.
* Feedback if spoken to in room not on whitelist.
* Allow jobs to be specified by project and job name, not just job ID.

## Contributing

Pull requests are welcome.

## Author

Pete Cornish (outofcoffee@gmail.com)
