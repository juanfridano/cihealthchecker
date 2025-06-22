# CI Health Checker for Camunda Repositories

This tool monitors the health of GitHub Actions workflows for a single repository. It currently fetches workflow runs from the https://github.com/camunda/camunda mono-repo and groups them by workflow name to summarize run statistics.

---

## 📸 Example Report

You can hit the `/report` endpoint to generate a report. Example output:
```
[
    {
        "workflowName": "[Legacy] Operate",
        "totalRuns": 26,
        "failures": 17,
        "failureRate": 65.38461538461539,
        "avgDurationMinutes": 13.76923076923077
    },
    {
        "workflowName": "PR #32794",
        "totalRuns": 1,
        "failures": 0,
        "failureRate": 0.0,
        "avgDurationMinutes": 14.0
    },
    {
        "workflowName": "PR #33961",
        "totalRuns": 1,
        "failures": 0,
        "failureRate": 0.0,
        "avgDurationMinutes": 15.0
    },
    {
        "workflowName": "PR #33364",
        "totalRuns": 1,
        "failures": 0,
        "failureRate": 0.0,
        "avgDurationMinutes": 14.0
    },
    {
        "workflowName": "PR #32474",
        "totalRuns": 1,
        "failures": 0,
        "failureRate": 0.0,
        "avgDurationMinutes": 11.0
    },
    {
        "workflowName": "PR #32793",
        "totalRuns": 1,
        "failures": 0,
        "failureRate": 0.0,
        "avgDurationMinutes": 14.0
    },
    {
        "workflowName": "PR #33442",
        "totalRuns": 1,
        "failures": 0,
        "failureRate": 0.0,
        "avgDurationMinutes": 14.0
    },
    {
        "workflowName": "Operate Tests",
        "totalRuns": 6,
        "failures": 3,
        "failureRate": 50.0,
        "avgDurationMinutes": 7.0
    }
]
```
---

## 🔧 Setup Instructions

### 1. Create a GitHub Token

To access the GitHub API, create a Personal Access Token:

* Go to [https://github.com/settings/tokens](https://github.com/settings/tokens)
* Click **Generate new token** → select **Classic** or **Fine-grained**
* Give it repo-level permissions for reading workflows
* Save the token somewhere safe

### 2. Add Token to `application.yml`

```
yml
cihealthchecker:
  github:
    token: YOUR_GITHUB_TOKEN
    owner: YOUR_ORG_OR_USERNAME # currently camunda
    repo: YOUR_REPO_NAME # currently camunda
```

Alternatively, use environment variables or Spring profiles.

### 3. Run Locally

```
./mvnw spring-boot:run
```

Then open: [http://localhost:8080/report](http://localhost:8080/report)

---

## 🔁 CI/CD Pipeline

This project uses GitHub Actions with the following jobs:

* **Build**: Compiles the code with Java 21
* **Test**: Runs unit tests
* **Sonar**: Static code analysis with SonarQube
* **Dependency Check**: Scans for known vulnerabilities
* **Docker**: Builds and pushes an image to Docker Hub
* **Renovate**: Keeps dependencies up to date

See `.github/workflows/ci.yml` for full configuration.

---

## 🚧 TODOs

### Must-Have

* [ ] Add GitHub token authentication validation endpoint
* [ ] Handle GitHub API pagination (currently limited to 100 results)
* [ ] Parameterize the report date range (query params or headers)
* [ ] Support multiple repositories via config or query param
* [ ] Include workflow duration trends (e.g., over time)

### CI/CD Enhancements

* [ ] Deploy to staging/production using GitHub Environments
* [ ] Add deployment jobs (Docker Compose, K8s, etc.)
* [ ] Push reports to Slack or email summary daily
* [ ] Use `docker-metadata-action` to tag Docker images semantically

### Extras

* [ ] Add Prometheus metrics endpoint (`/actuator/prometheus`)
* [ ] Generate reports in CSV or JSON
* [ ] Add frontend to visualize reports better

---

## 📦 Docker Build Locally

```
./mvnw clean package
JAR_NAME=$(basename $(ls target/*.jar))
docker build --build-arg JAR_NAME=$JAR_NAME -t yourname/ci-health-checker:latest .
docker run -p 8080:8080 yourname/ci-health-checker:latest
```

---

## ✍️ Author

Created by Juan Fernandez Ridano 
