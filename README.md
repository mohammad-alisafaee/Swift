# Gradle

We can generate `gradlew` and `gradle` directory by `gradle wrapper --gradle-version 9.3.1`.
Before running the wrapper, we need to have Gradle installed using `brew install gradle`.
Note that `gradlew` won't work without `gradle/wrapper/gradle-wrapper.jar` jar file, so either commit both `gradlew` file and `gradle` directory, or none of them.


# Build

Use `build` task to build and also runs tests/checks.

```shell
./gradlew clean build
```

The `buildPlugin` task is a packaging task (it produces the installable jar file). To package, Gradle must first build whatever inputs it needs (compile, process resources, jar, patch plugin XML, etc.). In other words, `buildPlugin` normally depends on the `build` output. So, running `buildPlugin` is enough to build the plugin. It just won't run tests/checks that the `build` task does.

```shell
./gradlew buildPlugin
```

Running `clean` task isn't normally needed when just editing files. It should run if Gradle version or configuration changes or there's a change in target platform (e.g. IntelliJ version).

```shell
./gradlew clean buildPlugin
```
