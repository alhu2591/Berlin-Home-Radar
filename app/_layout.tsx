import { ActivityIndicator, View } from 'react-native';
import { Stack, usePathname, useRouter } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';

import { useBootstrap } from '@/hooks/useBootstrap';
import { tokens } from '@/design/tokens';
import { usePreferencesStore } from '@/stores/preferencesStore';

export default function RootLayout() {
  const { ready } = useBootstrap();
  const router = useRouter();
  const pathname = usePathname();
  const hasCompletedOnboarding = usePreferencesStore((state) => state.hasCompletedOnboarding);

  useEffect(() => {
    if (!ready) return;
    if (!hasCompletedOnboarding && pathname !== '/language-select' && pathname !== '/onboarding') {
      router.replace('/language-select');
    }
  }, [ready, hasCompletedOnboarding, pathname, router]);

  if (!ready) {
    return (
      <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: tokens.colors.background }}>
        <ActivityIndicator size="large" color={tokens.colors.primary} />
      </View>
    );
  }

  return (
    <>
      <StatusBar style="dark" />
      <Stack screenOptions={{ headerShown: false }}>
        <Stack.Screen name="(tabs)" />
        <Stack.Screen name="language-select" />
        <Stack.Screen name="onboarding" />
        <Stack.Screen name="listing/[id]" />
        <Stack.Screen name="alerts/[id]/results" />
        <Stack.Screen name="sources/index" />
        <Stack.Screen name="sources/add" />
        <Stack.Screen name="sources/catalog" />
        <Stack.Screen name="sources/[id]/index" />
        <Stack.Screen name="sources/[id]/edit" />
        <Stack.Screen name="settings/about" />
        <Stack.Screen name="settings/privacy" />
        <Stack.Screen name="settings/insights" />
        <Stack.Screen name="settings/diagnostics" />
        <Stack.Screen name="settings/source-health" />
        <Stack.Screen name="settings/runtime" />
        <Stack.Screen name="settings/backup" />
        <Stack.Screen name="listing/[id]/notes" />
        <Stack.Screen name="compare" />
        <Stack.Screen name="map" />
        <Stack.Screen name="+not-found" />
      </Stack>
    </>
  );
}
