### Changelog interface
The changelog interface is a simple java interface located at `jp.co.soramitsu.bootstrap.changelog.ChangelogInterface`. The interface must be implemented in order to write changelog scripts in groovy.
## Interface structure
Methods to implement:

1) `String getSchemaVersion()` - returns changelog schema version. Every changelog script must have it's own unique schema version. Due to changelog service idempotentency, non-unique schema version changelogs won't be executed. 
