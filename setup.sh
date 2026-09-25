#!/usr/bin/env bash
# One-time setup for running the tests locally: a JDK, the Android SDK and an emulator.
# Anything already installed is left as is.
set -euo pipefail
cd "$(dirname "$0")"

# macOS has a `java` stub even without a JDK, hence the -version check.
if [[ -z ${JAVA_HOME:-} ]] && ! java -version >/dev/null 2>&1; then
  [[ -d $HOME/.sdkman ]] || curl -fsSL https://get.sdkman.io | bash
  set +u
  source "$HOME/.sdkman/bin/sdkman-init.sh"
  # 25 is what the Gradle daemon asks for (gradle/gradle-daemon-jvm.properties), so it needs no second JDK.
  yes | sdk install java 25.0.4-tem
  set -u
  export JAVA_HOME=$HOME/.sdkman/candidates/java/current
fi

# The SDK is looked up the way the Android Gradle plugin does it, falling back to the standard location.
sdk=$(sed -n 's/^sdk.dir=//p' local.properties 2>/dev/null || true)
sdk=${sdk:-${ANDROID_HOME:-$([[ $(uname) == Darwin ]] && echo "$HOME/Library/Android/sdk" || echo "$HOME/Android/Sdk")}}
grep -qs '^sdk.dir=' local.properties || echo "sdk.dir=$sdk" >> local.properties

tools="$sdk/cmdline-tools/latest/bin"
if [[ ! -x $tools/sdkmanager ]]; then
  # Google publishes no stable link to the latest command-line tools, so the builds are pinned.
  archive=$([[ $(uname) == Darwin ]] && echo commandlinetools-mac-15641748_latest.zip || echo commandlinetools-linux-16111833_latest.zip)
  tmp=$(mktemp -d)
  curl -fsSL "https://dl.google.com/android/repository/$archive" -o "$tmp/tools.zip"
  unzip -q "$tmp/tools.zip" -d "$tmp"
  mkdir -p "$sdk/cmdline-tools"
  mv "$tmp/cmdline-tools" "$sdk/cmdline-tools/latest"
  rm -rf "$tmp"
fi

abi=$([[ $(uname -m) == arm64 || $(uname -m) == aarch64 ]] && echo arm64-v8a || echo x86_64)
# The same API level as the CI emulator.
image="system-images;android-37.0;google_apis;$abi"
yes | "$tools/sdkmanager" --licenses >/dev/null
"$tools/sdkmanager" platform-tools emulator "$image"
if ! "$sdk/emulator/emulator" -list-avds | grep -qx tictactoe; then
  echo no | "$tools/avdmanager" create avd -n tictactoe -k "$image" -d pixel_7
fi
echo "Start the emulator with: $sdk/emulator/emulator -avd tictactoe"
