cd "$(dirname "$0")" || exit 1

# Prefer JAVA_HOME if set, otherwise use java from PATH
if [ -n "$JAVA_HOME" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA="java"
fi

exec "$JAVA" \
    -Dlog4j.logger.level=INFO \
    -jar wattzap.jar "$@"
