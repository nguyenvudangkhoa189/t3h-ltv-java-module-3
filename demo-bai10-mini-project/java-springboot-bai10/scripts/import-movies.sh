#!/usr/bin/env bash
# Import dataset Netflix theo PDF Bài 7 (trang 4)
# Tải file từ: https://www.kaggle.com/datasets/rahulverma07/netflix-movie-dataset
# Đặt file CSV (đổi tên mymoviedb.csv) cạnh script hoặc truyền đường dẫn làm tham số $1

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
CSV_FILE="${1:-$SCRIPT_DIR/../sample-data/mymoviedb.csv}"

if [[ ! -f "$CSV_FILE" ]]; then
  echo "Không tìm thấy file: $CSV_FILE"
  echo "Tải dataset Kaggle và chạy: $0 /path/to/mymoviedb.csv"
  exit 1
fi

echo "Importing $CSV_FILE -> db_java_t3h_module3.mymoviedb ..."
mongoimport \
  --db db_java_t3h_module3 \
  --collection mymoviedb \
  --type csv \
  --headerline \
  --drop \
  --file "$CSV_FILE"

echo "Done. Kiểm tra: mongosh db_java_t3h_module3 --eval 'db.mymoviedb.countDocuments()'"
