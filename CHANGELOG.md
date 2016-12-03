# Change Log

All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [0.6.2] - 2016-12-03
### Added
- Adds changelog (this file!)

### Fixed
- Fixes template resolution when more than one candidate remains but one is already satisfied.
- Removes ‘roles’ from being mandatory in custom security configuration.

## [0.6.1] - 2016-11-20
### Added
- Adds grouped action start/complete messages.
- Sends join message on connection.
- Permit commands ending with colon after bot persona ID.
- Adds timeout to queued item check.

## [0.6.0] - 2016-11-16
### Added
- Adds lockable option values.
- Sorts action templates by name in help.
- Makes user-specified options override static values.

### Changed
- Stops printing usage on receipt of unknown command.

## [0.5.0] - 2016-11-14
### Added
- Adds Jenkins driver. Includes support for triggering jobs, queued items and status polling.
- Moves common driver functionality into shared modules.
- Factors out Rundeck support into separate driver module.
- Supports Jenkins instances with CSRF enabled or disabled.
- Makes trigger template matching case insensitive.

## [0.4.1] - 2016-11-06
### Added
- Adds option transformers.
- Allows roles to be restricted by tag.

## [0.4.0] - 2016-11-03
### Added
- Enables listening to multiple channels.
- Adds user and role authorisation framework.
- Adds 'help' action.
- Caches configuration for improved performance.

## [0.3.0] - 2016-10-30
### Added
- Adds support for multiple action commands, using tags.
- Polls for job status and reports back to channel.

## [0.2.0] - 2016-10-28
### Added
- Adds basic lock and unlock.
- Adds enable/disable (requires correct ACL).
- Adds status action.

## [0.1.0] - 2016-10-27
### Added
- Initial release.
- Rundeck support.
- Slack bot integration.
