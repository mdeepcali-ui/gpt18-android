#!/bin/sh
##############################################################################
# Gradle wrapper (simplified)
##############################################################################

APP_HOME=$(cd "${0%/*}" && pwd)
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

DEFAULT_JVM_OPTS="-Xmx1024m -Xms128m"

exec java $DEFAULT_JVM_OPTS $JAVA_OPTS -Dorg.gradle.appname=gradlew -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
