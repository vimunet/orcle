### **Jira Note: Set Up Periodic GitLab CI Pipeline for Syncing Bitbucket to GitLab**

**Objective:**
Configure a GitLab CI pipeline to periodically sync a Bitbucket repository to an on-prem GitLab repository by using **Pipeline Schedules**.

---

### **Steps:**

#### **1. Define the GitLab CI Pipeline in `.gitlab-ci.yml`**

Ensure the `.gitlab-ci.yml` file contains the pipeline definition for syncing Bitbucket with GitLab using HTTPS with Personal Access Tokens (PATs).

Example `.gitlab-ci.yml`:
```yaml
stages:
  - sync

sync_job:
  stage: sync
  script:
    # Clone the Bitbucket repository using HTTPS with the PAT
    - git clone --bare https://$BITBUCKET_USERNAME:$BITBUCKET_TOKEN@bitbucket.org/username/repository.git
    - cd repository.git

    # Add GitLab as a remote using HTTPS with the PAT
    - git remote add gitlab https://oauth2:$GITLAB_TOKEN@gitlab.company.com/username/repository.git

    # Push all branches and tags to GitLab
    - git push --mirror gitlab
```

Ensure you’ve added these variables under **CI/CD Variables**:
- `BITBUCKET_USERNAME`
- `BITBUCKET_TOKEN`
- `GITLAB_TOKEN`

#### **2. Create a Pipeline Schedule in GitLab**

1. **Go to Your GitLab Project**:
   - Navigate to the GitLab project that contains your `.gitlab-ci.yml` file.

2. **Go to CI/CD Settings**:
   - Navigate to **CI/CD** > **Schedules**.

3. **Create a New Pipeline Schedule**:
   - Click on **New schedule**.

4. **Configure the Schedule**:
   - **Description**: Add a descriptive name (e.g., "Sync Bitbucket to GitLab").
   - **Interval Pattern**: Set the cron-like schedule (e.g., every 15 minutes: `*/15 * * * *`).
   - **Target Branch**: Select the branch on which to run the pipeline (e.g., `master`).
   - **Timezone**: Set the appropriate timezone for your schedule.
   - Ensure the schedule is active.

5. **Save the Schedule**:
   - Click **Save pipeline schedule**.

#### **3. Verify and Monitor the Scheduled Pipelines**

- The configured pipeline will now run automatically at the specified intervals.
- You can monitor pipeline executions under **CI/CD** > **Pipelines**.

#### **Example Cron Patterns for Scheduling**:
- Every 15 minutes: `*/15 * * * *`
- Every hour: `0 * * * *`
- Every day at midnight: `0 0 * * *`
- Every Sunday at 3 AM: `0 3 * * 0`

---

This Jira note outlines the process of setting up a GitLab CI pipeline that runs periodically to automate repository synchronization.
