To loop over a list of services in a GitLab CI/CD pipeline only if the services are defined, you can check whether the variable (representing the services) is empty before executing the loop. Here’s how to do it:

### Example `.gitlab-ci.yml`

```yaml
variables:
  SERVICES: "api ui database"  # Define your services as a space-separated string
  # SERVICES: ""  # Uncomment this line to test with no services defined

stages:
  - deploy

deploy_services:
  stage: deploy
  script:
    - if [ -n "$SERVICES" ]; then  # Check if SERVICES is not empty
        IFS=' '  # Set Internal Field Separator to space
        for SERVICE in $SERVICES; do
          echo "Deploying $SERVICE..."
          # Add your deploy script or commands here
          echo "Finished deploying $SERVICE!"
        done
      else
        echo "No services defined for deployment.";
      fi
  only:
    - master
```

### Explanation:

1. **Variable Definition**:
   - `SERVICES` is defined as a space-separated string. You can change this to an empty string to test the condition where no services are defined.

2. **Check if Services are Defined**:
   - The `if [ -n "$SERVICES" ]; then` statement checks if the `SERVICES` variable is not empty.
   - The `-n` flag checks for non-zero length, meaning it checks if the variable has any content.

3. **Loop through Services**:
   - If `SERVICES` is not empty, it sets the `IFS` (Internal Field Separator) to a space, allowing the loop to iterate over each service.
   - The `for SERVICE in $SERVICES; do` statement loops over each service and executes the commands inside the loop.

4. **Else Clause**:
   - If `SERVICES` is empty, it outputs a message saying "No services defined for deployment."

### Usage:
This setup ensures that the deployment commands are executed only if there are services defined in the `SERVICES` variable. If you leave the `SERVICES` variable empty or undefined, it will skip the loop and notify you accordingly.

### Testing:
You can test this by defining the `SERVICES` variable with different values or leaving it empty. The job will behave as expected based on whether services are defined or not.
