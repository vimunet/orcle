In GitLab CI/CD YAML files, you might encounter issues with multi-line scripts and indentation that can lead to the error "implicit keys need to be on a single line." To avoid this and ensure that your shell script runs correctly, you can use a block style for the script section.

Here's how to format your `.gitlab-ci.yml` to ensure that the `if` statement and the loop work without syntax errors:

### Corrected `.gitlab-ci.yml`

```yaml
variables:
  # CONTAINERS: "container1,container2,container3"  # Uncomment to test with defined containers

stages:
  - deploy
  - notify

deploy_containers:
  stage: deploy
  script:
    - |
      if [ -n "$CONTAINERS" ]; then
        echo "Deploying the following containers: $CONTAINERS"
        IFS=',' read -r -a container_array <<< "$CONTAINERS"  # Split CONTAINERS into an array
        for CONTAINER in "${container_array[@]}"; do  # Iterate over the array
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

### Key Changes:

1. **Using Block Style with `|`**:
   - The `- |` syntax allows you to write multi-line scripts without indentation issues. This tells YAML to treat everything following it as a single block of text.

2. **Proper Indentation**:
   - Ensure that each line in the script block is indented correctly for clarity and to avoid syntax errors.

3. **All Commands in One Block**:
   - All commands, including `if`, loops, and echo statements, are placed within the block, ensuring they are treated as a single unit by YAML.

### Behavior:
- **When `CONTAINERS` is Defined**: The `deploy_containers` job will run and deploy each container listed in the `CONTAINERS` variable.
- **When `CONTAINERS` is Not Defined**: The `notify_no_containers` job will run, indicating that no containers are defined.

This format should resolve any implicit key issues and allow your script to execute correctly in the GitLab CI/CD environment! If you have any more questions or need further assistance, feel free to ask!
