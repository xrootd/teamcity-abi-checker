abi-checker
==========

TeamCity plugin for checking ABI compatibility with previous builds.

Usage
-----

* Run `ant dist` in the root directory
* Copy `dist/abi-checker.zip` to `<teamcity user home directory>/.BuildAgent/plugins`
* Restart TeamCity server (`<teamcity installation directory>/bin/runAll.sh [start|stop]`)

Compatibility
-------------

Compatible with TeamCity 7.x. Not tested on earlier versions.

*Note:* Guest user login must be enabled in your TeamCity configuration for this plugin to work.
