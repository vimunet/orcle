To configure **Logback** to send logs to **AWS CloudWatch Logs** using a CloudWatch appender, you can use the **AWS CloudWatch Appender** for Logback. This appender allows you to send logs from your Java application directly to CloudWatch.

Here’s how you can set it up:

### **Step 1: Add the Dependency**
First, you need to add the required dependency for the CloudWatch Logback appender in your `pom.xml` (for Maven) or `build.gradle` (for Gradle) file.

For **Maven**:
```xml
<dependency>
    <groupId>io.github.rahulsom</groupId>
    <artifactId>cloudwatch-logback-appender</artifactId>
    <version>0.1.5</version>
</dependency>
```

For **Gradle**:
```groovy
implementation 'io.github.rahulsom:cloudwatch-logback-appender:0.1.5'
```

### **Step 2: Configure Logback with CloudWatch Appender**
Next, configure your `logback.xml` (usually located in the `src/main/resources` folder) to include the CloudWatch appender.

Example `logback.xml`:
```xml
<configuration>
    <appender name="CLOUDWATCH" class="io.github.rahulsom.cloudwatch.CloudWatchAppender">
        <logGroupName>log-group-1</logGroupName> <!-- CloudWatch Log Group 1 -->
        <logStreamName>app-log-stream</logStreamName> <!-- CloudWatch Log Stream -->
        <awsRegion>us-east-1</awsRegion>
        <awsAccessKeyId>${CLOUDWATCH_AWS_ACCESS_KEY_ID}</awsAccessKeyId>
        <awsSecretKey>${CLOUDWATCH_AWS_SECRET_ACCESS_KEY}</awsSecretKey>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Another CloudWatch Appender for second log group -->
    <appender name="CLOUDWATCH-2" class="io.github.rahulsom.cloudwatch.CloudWatchAppender">
        <logGroupName>log-group-2</logGroupName> <!-- CloudWatch Log Group 2 -->
        <logStreamName>app-log-stream-2</logStreamName> <!-- CloudWatch Log Stream -->
        <awsRegion>us-east-1</awsRegion>
        <awsAccessKeyId>${CLOUDWATCH_AWS_ACCESS_KEY_ID}</awsAccessKeyId>
        <awsSecretKey>${CLOUDWATCH_AWS_SECRET_ACCESS_KEY}</awsSecretKey>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Root logger to log to both CloudWatch Log Groups -->
    <root level="info">
        <appender-ref ref="CLOUDWATCH" />
        <appender-ref ref="CLOUDWATCH-2" />
    </root>
</configuration>
```

### **Step 3: Set Up AWS Credentials**
You can provide your AWS credentials in multiple ways:
1. **Hardcoded in `logback.xml`** (not recommended for production environments):
   ```xml
   <awsAccessKeyId>YOUR_ACCESS_KEY_ID</awsAccessKeyId>
   <awsSecretKey>YOUR_SECRET_ACCESS_KEY</awsSecretKey>
   ```

2. **Use environment variables**:
   - Set environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` in your ECS task definition or container.
   
3. **IAM Role**:
   - If you are running the application on ECS, it’s better to attach an **IAM role** to your ECS task or container that has permissions to write to CloudWatch Logs.
   - The appender will automatically use the credentials from the IAM role without the need for hardcoding access keys.

### **Step 4: Permissions**
Ensure the IAM role or AWS user has the following permissions for CloudWatch Logs:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents",
        "logs:DescribeLogStreams"
      ],
      "Resource": "*"
    }
  ]
}
```

### **Step 5: ECS Task Definition Logging Configuration**
If your application is running on ECS, ensure that your ECS task definition has the correct permissions and environment variables set for accessing AWS CloudWatch Logs.

### Conclusion
With this setup, your application will log to two CloudWatch log groups using two separate appenders. You can configure the logging format and behavior according to your application’s needs. Make sure to secure your AWS credentials by using environment variables or IAM roles when running on ECS or other AWS services.
