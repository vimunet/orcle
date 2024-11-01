To write warnings and errors to different appenders in Logback, you can configure separate appenders and use a combination of `ThresholdFilter` and `LevelFilter` to direct logs based on severity. Here’s how you can set it up in your `logback.xml` configuration file:

```xml
<configuration>
    <!-- Define appender for warnings -->
    <appender name="WARN_FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/warnings.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>WARN</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- Define appender for errors -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/errors.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- Set root logger to INFO level and add both appenders -->
    <root level="INFO">
        <appender-ref ref="WARN_FILE" />
        <appender-ref ref="ERROR_FILE" />
    </root>
</configuration>
```

### Explanation
- **`WARN_FILE` appender**: Captures only `WARN` level logs using `LevelFilter` with `<level>WARN</level>`.
- **`ERROR_FILE` appender**: Captures only `ERROR` level logs using `LevelFilter` with `<level>ERROR</level>`.
- **Root logger**: Routes all log messages to the defined appenders. The `INFO` level allows the appender filters to manage what gets logged at each level.

With this setup:
- All warnings are logged to `warnings.log`.
- All errors are logged to `errors.log`.
