#!/bin/bash
# Run this script to generate the Langoa release keystore.
# You will be prompted for the store password, key password, and details.
keytool -genkey -v \
  -keystore "$(dirname "$0")/langova-release.jks" \
  -alias langova \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
