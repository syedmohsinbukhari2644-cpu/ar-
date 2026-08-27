# Beauty Camera Tester

## Architecture

- `android/` is a standalone Kotlin application. The Expo Router source is preserved for comparison and JavaScript development, but native APK builds do not invoke Metro or Expo prebuild.
- CameraX provides the front-camera preview, runtime permission flow, and latest-frame analysis backpressure.
- MediaPipe Tasks Vision Face Landmarker runs in live-stream mode from `face_landmarker.task`; landmarks drive a transparent OpenGL ES 3 overlay.
- The renderer applies a face-bounded beauty shader with beauty, brightness, and warmth controls. `PixelCopy` saves the visible composite to Pictures/BeautyCameraTester.

## Package List

- `beautycameratester`
- `src/app/index.tsx`
- `src/app/_layout.tsx`
- Android Gradle Plugin `8.6.1`, Kotlin `2.0.21`, Gradle `8.7`
- compile/target SDK `35`
- CameraX `1.4.2`, MediaPipe Tasks Vision `0.10.26`

## Build Steps

1. Install Node.js LTS, Android Studio, Android SDK Platform 35, Android SDK Build-Tools, and a physical Android device with USB debugging enabled.
2. Run `npm install` from this folder.
3. Set Android Studio's Gradle JDK to JDK 17.
4. From `android/`, run `gradlew.bat assembleDebug`. The model must exist at `android/app/src/main/assets/face_landmarker.task`.

## Implementation Phases

1. Native CameraX permission flow and front-camera preview.
2. MediaPipe Face Landmarker live-stream inference.
3. OpenGL ES 3 face-bounded beauty shader and controls.
4. Performance labels, screenshot saving, lifecycle cleanup, and debug APK workflow.
5. Improve segmentation, feature protection, temporal smoothing, and device calibration.

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

- The workflow downloads the official model because the binary is intentionally not committed. Offline builds must download it manually into the assets directory.
- The current shader uses a smooth face ellipse derived from landmarks; feature-specific holes and segmentation remain future quality work.
- Expo Go continues to run the preserved JavaScript source, while the native APK is the functional CameraX prototype.