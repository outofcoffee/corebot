# HTTP Slack bot

## Configuration

Set the following environment variables:

 - SLACK_CHANNEL_MEMBERS - users to invite e.g. janesmith,bob
 - SLACK_USER_TOKEN - must have the following permission scopes:
   - users:read
   - groups:read
   - groups:write
   - chat:write:bot
 - HTTP_BIND_PORT (default 8080)
 - ACTION_CONFIG_FILE - an empty action configuration file - see `examples/config/empty-action-config.yml`

## Usage

Usage: `./bots-http-slack`

> Note: The bot starts on port 8080 by default.

### Examples

Display help:

    http://localhost:8080/help

Forward message to channel:

    http://localhost:8080/forwardMessage?message=qux&channel=bot-test4
