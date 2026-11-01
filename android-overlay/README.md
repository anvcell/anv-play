# AnV Play Android TV Overlay

A small transparent top-right countdown overlay for Android/Google TV. It listens to Firestore `unit_status` documents and displays a 60-second warning before the rental end time.

## Build
GitHub Actions builds a debug APK and uploads it as an artifact. The Android TV must grant the app permission to display over other apps.

## Important limitation
Some TVs do not composite third-party Android overlays over HDMI input. The overlay must be tested with a PS3/PS4 HDMI source. At zero, the app displays a full-screen end notice; the existing laptop ADB bridge remains responsible for putting the TV display to sleep.

## Firebase
Uses the existing AnV Play Firebase project. Firestore read access is subject to the project's security rules.