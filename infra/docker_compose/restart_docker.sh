#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"
json_file="$SCRIPT_DIR/config/browsers.json"

echo ">>> Остановить Docker Compose"
docker compose down

echo ">>> Docker pull все образы браузеров"

# Проверяем, что jq установлен
if ! command -v jq &> /dev/null; then
    echo "❌ jq is not installed. Please install jq and try again."
    exit 1
fi

# Извлекаем все значения .image через jq
if [ ! -f "$json_file" ]; then
    echo "browsers.json not found at: $json_file"
    exit 1
fi
images=$(jq -r '.. | objects | select(.image) | .image' "$json_file")

# Пробегаем по каждому образу и выполняем docker pull
for image in $images; do
    echo "Pulling $image..."
    docker pull "$image"
done

echo ">>> Запуск Docker Compose"
docker compose up -d
