# Project Configuration (read before building from source)
Stonky is now a _kotlin-multiplatform_ project. It contains both a _server_ and an _android client_.

As of this release, to build _any platform_ locally you **MUST** have
- A valid `config.yaml`
- A download of the android sdk, with it's path set in `local.properties`

Configuration is done via the `config.yaml` file, which should be made
 following the `example.config.yaml` file in the root project directory.

The `local.properties` file is currently required due to an obligate
 inclusion of the android sdk due to the android gradle plugin.
This will be fixed in a future update so that server can be built without the
 dependence on the android sdk.

**local.properties**
```
sdk.dir='/path/to/android/sdk'
```


## IntelliJ Project
Three run configurations are included to make deployment easier from within IntelliJ IDEA.
The two stand alone configurations are:
- stonky.android.app, which allows for quick rebuilds of the android client, without halting any running server instances.
- stonky.server, which allows for updates to the server in a similar fashion
The final configuration is simply "stonky" which simultaneously executes all stand alone configurations.


## Structure
The _android_ module is currently being integrated fully into the project,
 but uses it's own gradle wrapper for the time being, to avoid refactoring
 costs and to preserve sub-project interoperability with AndroidStudio.

### The project structure is as follows:

**stonky**
```
.
|—- buildSrc
|—- config
|—- android
    |-- app
    |-- database
    |-- graph
    |-- repos
    |-- theme
|—- common
    |-- src
        |-- commonMain
        |-- androidMain
        |-- jsMain
        |-- serverMain
|—- server
    |-- src
        |-- commonMain
        |-- iosArm64Main
        |-- iosX64Main
        |-- jvmMain
|-- idea
    |-- runConfigurations
|
| ...
|
|-- config.yaml
|-- local.properties
```

The `config` module uses kotlinpoet to generate a shared kotlin config file.
This file is used to ensure that the API keys and endpoints match across all platforms.

The `idea` folder is deliberately separate from the `.idea` folder to put emphasis on the custom build configurations.
This is to provide contrast to the standard case, as the `.idea` folder is meant to be committed to the repository (without workspace.xml).


### Building
The jar for the project can be updated from source by running the gradle ShadowJar gradle task (use `gradlew.bat` on Windows).
```
$ gradlew :server:shadowJar
```

The jar is then output in `/server/build/libs/` as `server-X.X.X-SNAPSHOT-all.jar`

It can then be run with `java -jar server-X.X.X-SNAPSHOT-all.jar`. Make sure to have a `config.yaml` in the directory you are executing in.