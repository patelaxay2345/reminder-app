The gradle-wrapper.jar file is intentionally not included in this project zip.

When you open this project in Android Studio, the IDE will automatically
fetch the correct wrapper jar on first sync.

If you prefer the command line, run this once from the project root after
installing Gradle locally (any 7.x or 8.x version works):

    gradle wrapper --gradle-version 8.4

That generates gradle-wrapper.jar, gradlew, and gradlew.bat.
