# Beauty Camera Tester

## Architecture

- Expo Router screen in `src/app/index.tsx` owns the Phase 1 camera lifecycle and controls shell.
- `expo-camera` provides the Android front-camera preview and runtime permission flow.
- Future phases will isolate MediaPipe landmark inference, coordinate transforms, face-mask construction, and OpenGL ES rendering in `src/mediapipe`, `src/model`, and `src/renderer`.
- Camera work remains on the native camera pipeline; no cloud service, backend, or JavaScript image-processing loop is used.

## Package List

- `beautycameratester`
- `src/app/index.tsx`
- `src/app/_layout.tsx`
- `expo-camera` `~57.0.4`
- `expo-router` `~57.0.17`
- `react-native` `0.86.3`
- `expo` `~57.0.17`

## Build Steps

1. Install Node.js LTS, Android Studio, Android SDK Platform 35, Android SDK Build-Tools, and a physical Android device with USB debugging enabled.
2. Run `npm install` from this folder.
3. Run `npx expo start`, then press `a` or scan the QR code with a development build.
4. For a native Android build, run `npx expo run:android` after Android Studio has configured `ANDROID_HOME` and a JDK 17 environment.

## Implementation Phases

1. Camera permission flow and front-camera preview.
2. MediaPipe Face Landmarker and debug landmarks.
3. Coordinate transforms and temporal smoothing.
4. OpenGL ES normal-camera renderer.
5. Face skin mask and protected feature holes.
6. GPU beauty shader and controls.
7. Performance metrics, screenshot, documentation, and debug APK verification.

## Acceptance Criteria

### Phase 1

- Android requests camera permission at runtime.
- Preview opens automatically after permission is granted.
- Front camera is selected by default and mirrored by CameraX/Expo.
- Permission denial and camera errors show a usable recovery state.
- Screen stays awake while the camera screen is active.
- The UI has disabled placeholders for future beauty controls without claiming the filter works.

### Final Prototype

- One-face MediaPipe tracking is stable under modest movement and distance changes.
- GPU skin-only smoothing protects eyes, brows, lips, teeth, nostrils, hair, and background.
- Beauty level zero is visually identical to the source preview.
- Performance is measured on a physical mid-range Android phone; no values are invented.

## Known Limitations At Phase 1

- No MediaPipe model or landmark overlay yet.
- No OpenGL beauty filter yet.
- Screenshot capture is not implemented yet; it must capture the visible GPU composite before release.
- Expo Go is useful for the initial camera screen, but later native GPU/vision work may require a development build.