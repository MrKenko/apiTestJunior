# TestNbank

Instructions for running tests locally and with Docker Compose.

## Prerequisites
- Java 21
- Docker (and Docker Compose v2)

## Local run (Maven)
1) Start required services (backend, frontend, selenoid):
```bash
cd infra/docker_compose
docker compose up -d
```

2) Run tests from the project root:
```bash
./mvnw -Papi test
./mvnw -Pui test
```

On Windows:
```powershell
.\mvnw.cmd -Papi test
.\mvnw.cmd -Pui test
```

## Run tests with Docker + Docker Compose
1) Start required services:
```bash
cd infra/docker_compose
docker compose up -d
```

2) Run tests in a Docker container from the project root:
```bash
./run-tests.sh api
./run-tests.sh ui
```

Test outputs are written to `test-output/<timestamp>_<profile>/` (logs, results, report).

## Useful environment variables (docker run script)
- `TEST_PROFILE` (default: `api`)
- `APIBASEURL` (default: `http://host.docker.internal:4111`)
- `UIBASEURL` (default: `http://172.23.96.1:3000`)
- `UIREMOTE` (default: `http://selenoid:4444/wd/hub`)
- `DOCKER_NETWORK` (optional)
