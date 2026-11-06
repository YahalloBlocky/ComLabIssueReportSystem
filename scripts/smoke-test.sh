#!/usr/bin/env bash
# Quick check against a RUNNING app. Usage: scripts/smoke-test.sh [http://localhost:8080]
B=${1:-http://localhost:8080}; J=$(mktemp)
echo "health:   $(curl -s $B/api/health)"
echo "no login: $(curl -s -o /dev/null -w '%{http_code}' $B/api/issues/mine) (expect 401)"
curl -s -c $J -H 'Content-Type: application/json' -d '{"schoolId":"202610001","password":"Student@123"}' $B/api/auth/login; echo
echo "student -> admin api: $(curl -s -b $J -o /dev/null -w '%{http_code}' $B/api/admin/dashboard) (expect 403)"
echo "my reports: $(curl -s -b $J $B/api/issues/mine | head -c 120)..."
rm -f $J
