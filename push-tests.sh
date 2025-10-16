#!/bin/bash
set -e  # Прерывать выполнение при любой ошибке

# === Настройки ===
IMAGE_NAME="nbank-tests"
DOCKERHUB_USERNAME="mrkenko"
TAG="latest"

# === Проверка наличия токена ===
if [ -z "$DOCKERHUB_TOKEN" ]; then
  echo "❌ Ошибка: переменная DOCKERHUB_TOKEN не задана."
  echo "Пример запуска:"
  echo "  DOCKERHUB_TOKEN=your_token ./push-tests.sh"
  exit 1
fi

echo ">>> Логин в Docker Hub"
echo "$DOCKERHUB_TOKEN" | docker login --username "$DOCKERHUB_USERNAME" --password-stdin

echo ">>> Тегирование образа"
docker tag "$IMAGE_NAME" "$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"

echo ">>> Отправка образа в Docker Hub"
docker push "$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"

echo "✅ Готово!"
echo "Образ доступен как:"
echo "  docker pull $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"
