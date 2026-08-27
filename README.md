# Beauty Camera Tester

The repository preserves the Expo source and now includes a separate, buildable native Kotlin Android app under `android/`. The native app uses CameraX, MediaPipe Tasks Vision Face Landmarker, an OpenGL ES 3 beauty overlay, runtime permission handling, controls, performance labels, lifecycle cleanup, and screenshot saving.

## Required software

- Node.js LTS
- Android Studio with Android SDK Platform 35, SDK Build-Tools, and Platform-Tools
- JDK 17 (Android Studio bundled JDK is recommended)
- Physical Android phone with USB debugging enabled

## Run on Android

```bash
npm install
npx expo start
```

The native debug build requires JDK 17 and uses the checked-in Gradle project:

```bash
cd android
gradlew.bat assembleDebug
```

The APK is written to `android/app/build/outputs/apk/debug/app-debug.apk`.

The Expo source remains runnable with `npx expo start`; it is not used by the native Gradle build.

## Build APK with GitHub Actions

Create a new GitHub repository, then from this project folder run:

```bash
git init
git add .
git commit -m "Initial Beauty Camera Tester"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
git push -u origin main
```

Open the repository's **Actions** tab, choose **Build Android APK**, and click **Run workflow**. When it finishes, open the workflow run and download the artifact named `beauty-camera-tester-debug-apk`. Extract it and install `app-debug.apk` on the phone.

The workflow downloads the official MediaPipe Face Landmarker model to `android/app/src/main/assets/face_landmarker.task`, then runs `gradlew assembleDebug`. No signing key is included; this is a debug APK for testing only. For offline builds, download that model into the same assets directory first.

## USB debugging

On the phone, open Settings > About phone and tap Build number seven times. In Developer options, enable USB debugging, connect the phone by USB, accept the RSA prompt, and verify with `adb devices`.

## Common errors

- `adb` not found: add Android SDK `platform-tools` to PATH.
- SDK or JDK errors: set Android Studio's Gradle JDK to its bundled JDK 17 and install the requested SDK platform.
- Permission denied: enable camera permission for Beauty Camera Tester in Android Settings, then relaunch.

## Current scope

The current native shader is a face-bounded prototype. Feature-specific protected holes, stronger temporal smoothing, and device performance calibration remain follow-up work.

This is an [Expo](https://expo.dev) project created with [`create-expo-app`](https://www.npmjs.com/package/create-expo-app).

## Get started

1. Install dependencies

   ```bash
   npm install
   ```

2. Start the app

   ```bash
   npx expo start
   ```

In the output, you'll find options to open the app in a

- [development build](https://docs.expo.dev/develop/development-builds/introduction/)
- [Android emulator](https://docs.expo.dev/workflow/android-studio-emulator/)
- [iOS simulator](https://docs.expo.dev/workflow/ios-simulator/)
- [Expo Go](https://expo.dev/go), a limited sandbox for trying out app development with Expo

You can start developing by editing the files inside the **app** directory. This project uses [file-based routing](https://docs.expo.dev/router/introduction).

## Get a fresh project

When you're ready, run:

```bash
npm run reset-project
```

This command will move the starter code to the **app-example** directory and create a blank **app** directory where you can start developing.

### Other setup steps

- To set up ESLint for linting, run `npx expo lint`, or follow our guide on ["Using ESLint and Prettier"](https://docs.expo.dev/guides/using-eslint/)
- If you'd like to set up unit testing, follow our guide on ["Unit Testing with Jest"](https://docs.expo.dev/develop/unit-testing/)
- Learn more about the TypeScript setup in this template in our guide on ["Using TypeScript"](https://docs.expo.dev/guides/typescript/)

## Learn more

To learn more about developing your project with Expo, look at the following resources:

- [Expo documentation](https://docs.expo.dev/): Learn fundamentals, or go into advanced topics with our [guides](https://docs.expo.dev/guides).
- [Learn Expo tutorial](https://docs.expo.dev/tutorial/introduction/): Follow a step-by-step tutorial where you'll create a project that runs on Android, iOS, and the web.

## Join the community

Join our community of developers creating universal apps.

- [Expo on GitHub](https://github.com/expo/expo): View our open source platform and contribute.
- [Discord community](https://chat.expo.dev): Chat with Expo users and ask questions.
