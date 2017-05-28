# Change Log

All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## Unreleased
### Added
- Adds support for external data stores. This includes things like action locks and option locks, but can support other types in future.
- Adds in-memory data store implementation, which is the default.
- Adds Redis data store implementation.

## [0.7.2] - 2017-05-23
### Fixed
- Fixes non-trigger events being sunk in action drivers.

## [0.7.1] - 2017-05-07
### Changed
- Only run as trigger user if explicitly set in action configuration.

## [0.7.0] - 2017-05-06
### Added
- Improves Jenkins error handling when obtaining CSRF token.
- Adds ability to run a Rundeck job as the initiating Slack user (thanks Anojan Sivarajah).
- Adds ability to show Rundeck job output in chat (thanks Anojan Sivarajah).
- Adds ability to handle spaces in arguments using quotes; Rundeck driver only (thanks Anojan Sivarajah).
- Adds CircleCI configuration.
- Adds a number of extension points.
- Adds Maven publishing configuration.

### Changed
- Bumps Gradle version. Bumps Kotlin version.
- Internal structural improvements.

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
