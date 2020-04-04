# Curse Modpack Downloader 2
This is a reimplementation of my CurseMeta3ModpackDownloader using a very similar UI but written completely in Kotlin.
This modpack downloader does not use curse meta 3.

## Installation Instructions
You must have Java 14 or later installed in order for this project to build correctly.

In order to build a redistributable package you must use the `jpackage` gradle task.

On Mac and Linux, this would look like:
```bash
./gradlew jpackage
```

On Windows, this would look like:
```bat
gradlew.bat jpackage
```

Once this has finished, you should find the application bundle in the `build/jpackage` directory.
