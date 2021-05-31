#!/usr/bin/env bash

set -eu

debug=${DEBUG:-no}
[[ ${debug} != no ]] && set -x

productsEndpoint="http://localhost:8080/v1/_/importer/products/"

for file in src/main/resources/data/product-*.json; do
  echo
  echo ">>> import product file: $file"
  curl -s ${productsEndpoint} -X POST --data @"${file}" --header 'Content-Type: application/json' | jq -c
done
