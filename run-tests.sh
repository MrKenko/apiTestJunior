#!/bin/bash

# Настройка
IMAGE_NAME=nbank-tests
TEST_PROFILE=${1:-api}
TIMESTAMP=$(date +"%Y%m%d_%H%M")
PROJECT_DIR=$(cd "$(dirname "$0")" && pwd -W)
TEST_OUTPUT_DIR="$PROJECT_DIR/test-output/$TIMESTAMP"

# Собираем Docker образ
echo ">>> Сборка запущена"
docker build -t $IMAGE_NAME .

mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

# Запуск докер контейнера
echo ">>> Тесты запущены"
docker run --rm \
 -v "$TEST_OUTPUT_DIR/logs:/app/logs" \
 -v "$TEST_OUTPUT_DIR/results:/app/target/surefire-reports" \
 -v "$TEST_OUTPUT_DIR/report:/app/target/site" \
 -e TEST_PROFILE="$TEST_PROFILE" \
 -e APIBASEURL=http://host.docker.internal:4111 \
 -e UIBASEURL=http://172.23.96.1:3000 \
 $IMAGE_NAME


 # Вывод итогов
 echo ">>> Тесты завершены"
 echo "Лог файл: $TEST_OUTPUT_DIR/logs/run.log"
 echo "Результаты тестов: $TEST_OUTPUT_DIR/results"
 echo "Репорт: $TEST_OUTPUT_DIR/report"