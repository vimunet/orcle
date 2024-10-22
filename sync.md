To set up the sync process between Bitbucket and GitLab as a cron job, you can use a script to mirror the repositories and then schedule it using `cron`. Here’s how you can do it:

### **Steps to Create a Cron Job for Bitbucket to GitLab Sync**

#### **1. Create a Shell Script for Sync**

Create a shell script that mirrors the Bitbucket repository to GitLab using HTTPS and Personal Access Tokens (PATs).

1. **Create the Shell Script (e.g., `sync-repos.sh`)**
   
   ```bash
   #!/bin/bash

   # Variables
   BITBUCKET_USERNAME="<your-bitbucket-username>"
   BITBUCKET_TOKEN="<your-bitbucket-pat>"
   GITLAB_TOKEN="<your-gitlab-pat>"
   BITBUCKET_REPO_URL="https://$BITBUCKET_USERNAME:$BITBUCKET_TOKEN@bitbucket.org/username/repository.git"
   GITLAB_REPO_URL="https://oauth2:$GITLAB_TOKEN@gitlab.company.com/username/repository.git"

   # Clone the Bitbucket repository as a bare repo
   git clone --bare $BITBUCKET_REPO_URL repo.git

   # Navigate into the cloned repo
   cd repo.git

   # Add GitLab as a remote
   git remote add gitlab $GITLAB_REPO_URL

   # Push all branches and tags to GitLab
   git push --mirror gitlab

   # Cleanup
   cd ..
   rm -rf repo.git
   ```

Make sure to replace:
- `<your-bitbucket-username>` with your Bitbucket username.
- `<your-bitbucket-pat>` with your Bitbucket Personal Access Token.
- `<your-gitlab-pat>` with your GitLab Personal Access Token.
- `username/repository.git` with the actual repository URLs.

2. **Save the Script:**
   - Save this script as `sync-repos.sh` (or any name you prefer) in a directory on your machine or server.
   - Make it executable:
     ```bash
     chmod +x /path/to/sync-repos.sh
     ```

#### **2. Set Up a Cron Job**

To run this script at regular intervals, you can use `cron`.

1. **Open the Crontab File**:
   - Use the following command to open the crontab for editing:
     ```bash
     crontab -e
     ```

2. **Add a New Cron Job**:
   - Add a line to the crontab to run the script at your desired frequency. For example, to run the sync every 15 minutes:
     ```bash
     */15 * * * * /path/to/sync-repos.sh >> /path/to/sync.log 2>&1
     ```
   
   This cron job will:
   - Run the `sync-repos.sh` script every 15 minutes.
   - Redirect output to `sync.log` so you can check the log file for any issues.

3. **Save and Exit**:
   - After editing the crontab, save and exit. The cron job will now run the sync script automatically at the specified interval.

---

### **Cron Scheduling Syntax:**
If you want different intervals, here are some examples:

- **Every hour**:  
  ```bash
  0 * * * * /path/to/sync-repos.sh >> /path/to/sync.log 2>&1
  ```
- **Every day at midnight**:  
  ```bash
  0 0 * * * /path/to/sync-repos.sh >> /path/to/sync.log 2>&1
  ```
- **Every Sunday at 3 AM**:  
  ```bash
  0 3 * * 0 /path/to/sync-repos.sh >> /path/to/sync.log 2>&1
  ```

### **Notes:**
- Make sure the machine or server running the cron job has `git` installed.
- Ensure the correct permissions are set for the script and directory.
- Check the cron job logs periodically (`sync.log`) to ensure the sync process is running smoothly.

Let me know if you need further adjustments!
