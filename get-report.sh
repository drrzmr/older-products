#!/usr/bin/env bash

set -eu

debug=${DEBUG:-no}
[[ ${debug} != no ]] && set -x

reportEndpoint="http://localhost:8080/v1/_/reports"

[[ x${1} == "x" ]] && echo "missing report uuid" && exit 1 || reportUUID=${1}

echo
curl -s "${reportEndpoint}/${reportUUID}"
