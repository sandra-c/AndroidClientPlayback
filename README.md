# AndroidClientPlayback   [![Release](https://jitpack.io/v/EricssonBroadcastServices/AndroidClientPlayback.svg)](https://jitpack.io/#EricssonBroadcastServices/AndroidClientPlayback)
 
* [Features](#features)
* [Tutorials](#tutorials)
* [License](https://github.com/EricssonBroadcastServices/AndroidClientPlayback/blob/master/LICENSE)
* [Requirements](#requirements)
* [Dependencies](#dependencies)
* [Installation](#installation)
* [Documentation](https://jitpack.io/com/github/EricssonBroadcastServices/AndroidClientPlayback/master-SNAPSHOT/javadoc/)
* [Release Notes](#release-notes)
* [Upgrade Guides](#upgrade-guides)

## Features

- [x] EMP Integration & Analytics
- [x] Base Playback Activity
- [x] Base EMPPlayer Layout
- [x] DASH VOD playback
- [x] DASH Live playback
- [x] DASH Catchup playback
- [x] DASH Offline playback
- [x] Support for Widevine DRM

## Tutorials

- [Integrating Simple Activity for playback](tutorials/tutorial-simple-player.md)
- [Customizing Player UI](tutorials/tutorial-player-customization.md)
- [Live and Catchup Playback](tutorials/tutorial-live-and-catchup-playback.md)
- [Custom Analytics and Entitlement Provider](tutorials/tutorial-custom-analytics-entitlement-provider.md)
- [Custom Playback Tech](tutorials/tutorial-custom-tech.md)

Contact the EMP team for tips on other types of player integrations :)

## Requirements

* `Android` 4.4+

## Dependencies

**Full**

- [AndroidClientAnalytics](https://github.com/EricssonBroadcastServices/AndroidClientAnalytics)
- [AndroidClientExposure](https://github.com/EricssonBroadcastServices/AndroidClientExposure)
- [AndroidClientUtilities](https://github.com/EricssonBroadcastServices/AndroidClientUtilities)

**Lightweight**

- [AndroidClientUtilities](https://github.com/EricssonBroadcastServices/AndroidClientUtilities)

## Installation

### JitPack
Releases are available on [JitPack](https://jitpack.io/#EricssonBroadcastServices/AndroidClientPlayback) and can be automatically imported to your project using Gradle.

Add the jitpack.io repository to your project **build.gradle**:
```gradle
allprojects {
 repositories {
    jcenter()
    maven { url "https://jitpack.io" }
 }
}
```

Then add the dependency to your module **build.gradle**:
```gradle
dependencies {
    compile 'com.github.EricssonBroadcastServices:AndroidClientPlayback:{version}'
}
```

Note: do not add the jitpack.io repository under *buildscript {}*

## Release Notes
Release specific changes can be found in the [CHANGELOG](CHANGELOG.md).

## Upgrade Guides
Major changes between releases will be documented with special [Upgrade Guides](UPGRADE_GUIDE.md).
