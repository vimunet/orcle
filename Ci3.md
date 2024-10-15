To achieve your requirement of using a comma-separated variable called `CONTAINERS` in GitLab CI/CD, where one job iterates over it if it's defined, and another stage runs only if the variable is not defined, you can set up your `.gitlab-ci.yml` file as follows:

### Example `.gitlab-ci.yml`

```yaml
variables:
  # CONTAINERS: "container1,container2,container3"  # Uncomment to test with defined containers

stages:
  - deploy
  - notify

deploy_containers:
  stage: deploy
  script:
    - IFS=','  # Set Internal Field Separator to comma
    - if [ -n "$CONTAINERS" ]; then
        echo "Deploying the following containers: $CONTAINERS"
        for CONTAINER in $CONTAINERS; do
          echo "Deploying $CONTAINER..."
          # Add your deploy script or commands here
          echo "Finished deploying $CONTAINER!"
        done
      else
        echo "No containers defined for deployment."
      fi
  rules:
    - if: '$CONTAINERS'  # This job runs only if CONTAINERS is defined
      when: on_success
    - when: never  # Prevent running if CONTAINERS is not defined

notify_no_containers:
  stage: notify
  script:
    - echo "No containers defined. Running notification stage."
  rules:
    - if: '!$CONTAINERS'  # This job runs only if CONTAINERS is not defined
      when: always
```

### Explanation:

1. **Variable Definition**:
   - `CONTAINERS` is declared but commented out. You can uncomment it and set it to a comma-separated string (e.g., `"container1,container2,container3"`) to test the deployment logic.

2. **Stages**:
   - There are two stages defined: `deploy` and `notify`.

3. **`deploy_containers` Job**:
   - **Script**: 
     - It sets `IFS=','` to split the `CONTAINERS` string by commas.
     - The `if [ -n "$CONTAINERS" ]` checks if `CONTAINERS` is defined and not empty.
     - If defined, it iterates over each container and executes the corresponding commands.
     - If not defined, it outputs "No containers defined for deployment."
   - **Rules**:
     - The job runs only if `CONTAINERS` is defined (`if: '$CONTAINERS'`).
     - The `when: never` prevents the job from running if `CONTAINERS` is not defined.

4. **`notify_no_containers` Job**:
   - **Script**: 
     - This job simply outputs a message indicating that no containers are defined.
   - **Rules**:
     - The job runs only if `CONTAINERS` is not defined (`if: '!$CONTAINERS'`).
     - The `when: always` ensures it runs whenever the condition is met.

### Behavior:
- **When `CONTAINERS` is Defined**: The `deploy_containers` job will run, iterating over the list of containers, while the `notify_no_containers` job will be skipped.
- **When `CONTAINERS` is Not Defined**: The `deploy_containers` job will be skipped, and the `notify_no_containers` job will run.

This setup effectively manages your requirements using rules in GitLab CI/CD!
