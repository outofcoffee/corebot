# websocket frontend

Ensure that you register the templates in `session-templates.yml` as well as these factories:

* `SetRealNameFactory`
* `SetUsernameFactory`
* `TerminateSessionFactory`

## Example usage

Start the bot. It listens on port 8025 by default.

## docker

Usage:

    $ docker run -it --rm joshgubler/wscat -c ws://docker.for.mac.localhost:8025
    connected (press CTRL+C to quit)
    > username pete
    < Username set to pete

## npm

Prerequisites:

    npm install -g wscat

Usage:

    $ wscat -c ws://localhost:8025
    connected (press CTRL+C to quit)
    > username pete
    < Username set to pete
