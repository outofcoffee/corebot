# websocket frontend

Ensure that you register the templates in `websocket-templates.yml` as well as these factories:

* `SetRealNameFactory`
* `SetUsernameFactory`

## Example usage

Prerequisites:

* wscat

Usage:

    $ wscat -c ws://localhost:8025
    connected (press CTRL+C to quit)
    > username pete
    < Username set to pete
