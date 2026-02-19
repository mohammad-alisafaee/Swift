# Swift

An IntelliJ plugin that provides syntax highlighting and code navigation for the Swift language.


## Gradle

Generate `gradlew` and the `gradle` directory by running:

```shell
gradle wrapper --gradle-version 9.3.1
```

Before running the wrapper, make sure Gradle is installed using `brew install gradle`.

Note that `gradlew` will not work without the `gradle/wrapper/gradle-wrapper.jar` JAR file. Either commit both the `gradlew` file and the `gradle` directory, or commit neither of them.


## Build

Use the `build` task to build the plugin and run tests/checks.

```shell
./gradlew clean build
```

The `buildPlugin` task is a packaging task that produces the installable JAR file. To package the plugin, Gradle first whatever inputs it needs (compile, process resources, JAR, patching plugin XML, and so on). In other words, `buildPlugin` normally depends on the `build` output; it just will not run the tests/checks that the `build` task does.

```shell
./gradlew buildPlugin
```

Running the `clean` task is not usually necessary when only editing files. It is mainly useful when the Gradle version or configuration changes, or when the target platform changes (for example, the IntelliJ version).

```shell
./gradlew clean buildPlugin
```


## Run the plugin in IntelliJ

There is no need to install the plugin in IntelliJ every time. We can run it in a separate IntelliJ instance using the `runIde` task. This will automatically install the plugin and start the IDE.

By default (that is, when running `./gradlew runIde`), this instance uses the `.build/idea-sandbox` directory for its configuration. This directory is deleted when running the `clean` task, which removes all settings for the test instance. To avoid this, we can set a different sandbox path. We can also configure this in `build.gradle` so we do not need to specify it every time.

```shell
./gradlew runIde -Pintellij.sandboxDir="$PWD/.intellij-temporary/"
```
