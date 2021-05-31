#!/usr/bin/env bash

set -eu

debug=${DEBUG:-no}
[[ ${debug} != no ]] && set -x

ordersEndpoint="http://localhost:8080/v1/_/importer/orders/"

for file in src/main/resources/data/order-*.json; do
  echo
  echo ">>> import order file: $file"
  curl -s ${ordersEndpoint} -X POST --data @"${file}" --header 'Content-Type: application/json' | jq -c
done
