# http frontend

Ensure that you register the templates in `session-templates.yml` as well as these factories:

* `SetRealNameFactory`
* `SetUsernameFactory`
* `TerminateSessionFactory`

## Example usage

Start the bot. It listens on port 8080 by default.

Parameterless action:

    curl http://localhost:8080/help

Action with parameters:

    curl http://localhost:8080/borrowItem?itemName=sit&reason=testing
