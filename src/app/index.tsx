import { CameraView, useCameraPermissions } from 'expo-camera';
import { useKeepAwake } from 'expo-keep-awake';
import { useEffect, useState } from 'react';
import {
  Alert,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

type CameraFacing = 'front' | 'back';

type SliderProps = {
  label: string;
  value: number;
  onChange: (value: number) => void;
};

export default function HomeScreen() {
  useKeepAwake();
  const [permission, requestPermission] = useCameraPermissions();
  const [cameraError, setCameraError] = useState<string | null>(null);
  const [facing, setFacing] = useState<CameraFacing>('front');
  const [beauty, setBeauty] = useState(35);
  const [brightness, setBrightness] = useState(5);
  const [warmth, setWarmth] = useState(5);
  const [beforeAfter, setBeforeAfter] = useState(false);

  useEffect(() => {
    if (permission && !permission.granted && permission.canAskAgain) {
      requestPermission();
    }
  }, [permission, requestPermission]);

  if (!permission) {
    return <LoadingState />;
  }

  if (!permission.granted) {
    return (
      <SafeAreaView style={styles.centered}>
        <Text style={styles.kicker}>BEAUTY CAMERA TESTER</Text>
        <Text style={styles.title}>Camera access is required</Text>
        <Text style={styles.message}>Allow camera access to start the live preview.</Text>
        <Pressable style={styles.primaryButton} onPress={requestPermission}>
          <Text style={styles.primaryButtonText}>Allow camera</Text>
        </Pressable>
        {!permission.canAskAgain && <Text style={styles.errorText}>Enable camera access in Android Settings.</Text>}
      </SafeAreaView>
    );
  }

  return (
    <View style={styles.screen}>
      <CameraView
        style={styles.camera}
        facing={facing}
        mirror={facing === 'front'}
        onCameraReady={() => setCameraError(null)}
        onMountError={(error) => setCameraError(error.message)}
      />
      <SafeAreaView style={styles.overlay} pointerEvents="box-none">
        <View style={styles.topBar}>
          <View>
            <Text style={styles.kicker}>BEAUTY CAMERA TESTER</Text>
            <Text style={styles.status}>LIVE CAMERA / PHASE 1</Text>
          </View>
          <Text style={styles.faceState}>NO FACE</Text>
        </View>

        <View style={styles.bottomPanel}>
          {cameraError ? (
            <View style={styles.errorBlock}>
              <Text style={styles.errorText}>Camera unavailable</Text>
              <Text style={styles.errorDetail}>{cameraError}</Text>
              <Pressable style={styles.secondaryButton} onPress={() => setCameraError(null)}>
                <Text style={styles.secondaryButtonText}>Retry</Text>
              </Pressable>
            </View>
          ) : (
            <>
              <View style={styles.panelHeader}>
                <Text style={styles.previewLabel}>LIVE PREVIEW</Text>
                <Text style={styles.metrics}>FPS --  |  DETECTOR -- ms</Text>
              </View>
              <Slider label="Beauty" value={beauty} onChange={setBeauty} />
              <Slider label="Brightness" value={brightness} onChange={setBrightness} />
              <Slider label="Warmth" value={warmth} onChange={setWarmth} />
              <View style={styles.actionRow}>
                <ActionButton label={beforeAfter ? 'AFTER' : 'BEFORE'} onPress={() => setBeforeAfter((value) => !value)} />
                <ActionButton label="FLIP" onPress={() => setFacing((value) => (value === 'front' ? 'back' : 'front'))} />
                <ActionButton
                  label="SAVE"
                  onPress={() => Alert.alert('Coming soon', 'Screenshot capture will be enabled with the GPU renderer.')}
                />
              </View>
              <Text style={styles.comingSoon}>Filter rendering starts after MediaPipe and GPU phases.</Text>
            </>
          )}
        </View>
      </SafeAreaView>
    </View>
  );
}

function Slider({ label, value, onChange }: SliderProps) {
  const [trackWidth, setTrackWidth] = useState(0);
  const updateFromTouch = (event: { nativeEvent: { locationX: number } }) => {
    if (!trackWidth) return;
    const nextValue = Math.round(Math.max(0, Math.min(100, (event.nativeEvent.locationX / trackWidth) * 100)));
    onChange(nextValue);
  };

  return (
    <View style={styles.control}>
      <View style={styles.controlHeader}>
        <Text style={styles.controlLabel}>{label}</Text>
        <Text style={styles.controlValue}>{value}</Text>
      </View>
      <View
        style={styles.track}
        onLayout={(event) => setTrackWidth(event.nativeEvent.layout.width)}
        onStartShouldSetResponder={() => true}
        onResponderGrant={updateFromTouch}
        onResponderMove={updateFromTouch}
      >
        <View style={[styles.trackFill, { width: `${value}%` }]} />
        <View style={[styles.thumb, { left: `${value}%` }]} />
      </View>
    </View>
  );
}

function ActionButton({ label, onPress }: { label: string; onPress: () => void }) {
  return (
    <Pressable style={styles.actionButton} onPress={onPress}>
      <Text style={styles.actionText}>{label}</Text>
    </Pressable>
  );
}

function LoadingState() {
  return (
    <View style={styles.centered}>
      <Text style={styles.loadingText}>Preparing camera</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: '#080808' },
  camera: { flex: 1 },
  overlay: { ...StyleSheet.absoluteFill, justifyContent: 'space-between' },
  topBar: { flexDirection: 'row', justifyContent: 'space-between', paddingHorizontal: 20, paddingTop: 12 },
  kicker: { color: '#f2c7a5', fontSize: 11, fontWeight: '700', letterSpacing: 1.4 },
  status: { color: '#ffffffaa', fontSize: 10, letterSpacing: 1, marginTop: 4 },
  faceState: { color: '#ffffffcc', fontSize: 10, fontWeight: '700', letterSpacing: 1 },
  bottomPanel: { backgroundColor: '#080808e8', borderTopColor: '#ffffff1c', borderTopWidth: StyleSheet.hairlineWidth, paddingHorizontal: 20, paddingTop: 16, paddingBottom: 12, gap: 12 },
  panelHeader: { flexDirection: 'row', justifyContent: 'space-between' },
  previewLabel: { color: '#ffffff99', fontSize: 10, letterSpacing: 1.2 },
  metrics: { color: '#ffffff70', fontSize: 9 },
  control: { gap: 7 },
  controlHeader: { flexDirection: 'row', justifyContent: 'space-between' },
  controlLabel: { color: '#fff', fontSize: 12 },
  controlValue: { color: '#ffffff99', fontSize: 11 },
  track: { height: 4, backgroundColor: '#ffffff38', borderRadius: 2, position: 'relative' },
  trackFill: { height: 4, backgroundColor: '#f2c7a5', borderRadius: 2 },
  thumb: { position: 'absolute', top: -4, width: 12, height: 12, marginLeft: -6, borderRadius: 6, backgroundColor: '#fff' },
  actionRow: { flexDirection: 'row', gap: 8, marginTop: 2 },
  actionButton: { flex: 1, alignItems: 'center', borderColor: '#ffffff38', borderWidth: 1, borderRadius: 5, paddingVertical: 10 },
  actionText: { color: '#fff', fontSize: 10, fontWeight: '700', letterSpacing: 1 },
  comingSoon: { color: '#ffffff70', fontSize: 10 },
  centered: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: '#080808', padding: 28, gap: 14 },
  title: { color: '#fff', fontSize: 26, fontWeight: '600', textAlign: 'center' },
  message: { color: '#ffffffaa', fontSize: 15, lineHeight: 22, textAlign: 'center' },
  primaryButton: { backgroundColor: '#f2c7a5', borderRadius: 5, paddingHorizontal: 22, paddingVertical: 13 },
  primaryButtonText: { color: '#16100d', fontWeight: '700' },
  secondaryButton: { alignSelf: 'flex-start', borderColor: '#f2c7a5', borderWidth: 1, borderRadius: 5, paddingHorizontal: 18, paddingVertical: 9 },
  secondaryButtonText: { color: '#f2c7a5', fontWeight: '600' },
  errorBlock: { gap: 8 },
  errorText: { color: '#ffb4a8', fontSize: 13 },
  errorDetail: { color: '#ffffff99', fontSize: 11 },
  loadingText: { color: '#ffffffaa', fontSize: 13 },
});
