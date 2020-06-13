# Auto App Updater
[![Bintray](https://api.bintray.com/packages/pcchin/auto-app-updater/com.pcchin.auto-app-updater/images/download.svg)](https://bintray.com/pcchin/auto-app-updater/com.pcchin.auto-app-updater/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.pcchin.auto-app-updater/auto-app-updater/badge.svg)](https://search.maven.org/artifact/com.pcchin.auto-app-updater/auto-app-updater)

## Library Info
An Android library that allows for easy updating of the app through endpoints such as GitHub, JSON, XML etc.

This works by getting the version info and APK download link from the desired endpoints,
 and downloading them to a set location on the device. The package installer app is then opened to installed the APK that we had just downloaded.
 The next time the updater is triggered, the old APKs would be deleted.
 
The main flow of the process would be as follows:
1. Create a Builder of the [AutoAppUpdater](/auto-app-updater/src/main/java/com/pcchin/auto_app_updater/AutoAppUpdater.java) instance
2. Set the update type and update version (through `setUpdateType` and `setUpdateVersion`)
3. Set the [UpdaterDialog](/auto-app-updater/src/main/java/com/pcchin/auto_app_updater/dialogs/UpdaterDialog.java) that will be shown
4. Add from existing [Endpoints](/auto-app-updater/src/main/java/com/pcchin/auto_app_updater) or add your own custom ones
5. Call `Builder.build()` to build the [AutoAppUpdater](/auto-app-updater/src/main/java/com/pcchin/auto_app_updater/AutoAppUpdater.java) instance
6. Call `updater.run()` whenever you wish to start the update check for the app
 
The bulk of the documentation can be found in the wiki.

## Installation
This library is available in JCenter and Maven Central (in the future). To install, you would need to include the following into your `project/build.gradle`:

```
implementation 'com.pcchin.auto-app-updater:auto-app-updater:1.0.0'
```

## Test endpoints
Certain repos are created on [GitHub](https://github.com/aau-test), [Gitea](https://git.pcchin.com/aau-test) and [GitLab](https://gitlab.com/aau-test) to test whether the endpoints are working.
 If you wish to gain access to the private repositories on those repos for testing purposes,
 just send me a message or open a issue on the repository and I can add you as a collaborator to those repositories.

## Contribution
Any contribution is welcome, feel free to add any issues or pull requests to the repository.

## License
This library is licensed under the [Apache 2.0 License](/LICENSE).

License for semantic versioning implementation:
```
The MIT License (MIT)

Copyright (c) 2014 Simon Taddiken

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```