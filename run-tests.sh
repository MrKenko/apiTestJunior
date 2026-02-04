#!/bin/bash

# Настройка
IMAGE_NAME=nbank-tests
TEST_PROFILE=${1:-api}
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"
if (cd "$SCRIPT_DIR" && pwd -W >/dev/null 2>&1); then
  PROJECT_DIR="$(cd "$SCRIPT_DIR" && pwd -W)"
fi

TEST_OUTPUT_DIR="$PROJECT_DIR/test-output/${TIMESTAMP}_${TEST_PROFILE}"

DOCKER_NETWORK="${DOCKER_NETWORK:-}"
APIBASEURL="${APIBASEURL:-http://host.docker.internal:4111}"
UIBASEURL="${UIBASEURL:-http://172.23.96.1:3000}"
UIREMOTE="${UIREMOTE:-http://selenoid:4444/wd/hub}"

cd "$SCRIPT_DIR"

# Собираем Docker образ
echo ">>> Сборка запущена"
if [ "${SKIP_BUILD:-0}" != "1" ]; then
  docker build -t "$IMAGE_NAME" .
fi

mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

DOCKER_NETWORK_ARG=()
if [ -n "$DOCKER_NETWORK" ]; then
  DOCKER_NETWORK_ARG=(--network "$DOCKER_NETWORK")
fi

# Запуск докер контейнера
echo ">>> Тесты запущены"
docker run --rm \
 "${DOCKER_NETWORK_ARG[@]}" \
 -v "$TEST_OUTPUT_DIR/logs:/app/logs" \
 -v "$TEST_OUTPUT_DIR/results:/app/target/surefire-reports" \
 -v "$TEST_OUTPUT_DIR/report:/app/target/site" \
 -e TEST_PROFILE="$TEST_PROFILE" \
 -e APIBASEURL="$APIBASEURL" \
 -e UIBASEURL="$UIBASEURL" \
 -e UIREMOTE="$UIREMOTE" \
 "$IMAGE_NAME"


 # Вывод итогов
 echo ">>> Тесты завершены"
 echo "Лог файл: $TEST_OUTPUT_DIR/logs/run.log"
 echo "Результаты тестов: $TEST_OUTPUT_DIR/results"
 echo "Репорт: $TEST_OUTPUT_DIR/report"
