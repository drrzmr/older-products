#!/usr/bin/env bash

set -e

debug=${DEBUG:-no}
[[ ${debug} != no ]] && set -x

reportEndpoint="http://localhost:8080/v1/_/reports/"
defaultIntervals=$(
  cat <<EOF
[
  { "begin": 1, "end": 3 },
  { "begin": 4, "end": 6 },
  { "begin": 7, "end": 12 },
  { "begin": 13, "end": -1 }
]
EOF
)

begin=${1:-''}
end=${2:-''}
intervals=${3:-${defaultIntervals}}

[[ -z ${begin} ]] && echo "missing begin date (first argument)" && exit 1 || begin=${1}
[[ -z ${end} ]] && echo "missing end date (second argument)" && exit 1 || begin=${1}

payload=$(
  cat <<EOF
{ "begin": "${begin}", "end": "${end}", "intervals": ${intervals} }
EOF
)

echo "${payload}" | jq -c
echo
curl -s ${reportEndpoint} -X POST --data "${payload}" --header 'Content-Type: application/json' | jq
