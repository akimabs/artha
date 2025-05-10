#!/bin/bash

./gradlew clean assembleRelease
cd app/build/outputs/apk/release/ || exit 1
adb uninstall com.example.artha
adb install app-release.apk
cd ../../../../../