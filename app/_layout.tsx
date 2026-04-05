import { ActivityIndicator, View } from 'react-native';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';

import { useBootstrap } from '@/hooks/useBootstrap';
import { tokens } from '@/design/tokens';

export default function RootLayout() {
  const { ready } = useBootstrap();

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
        <Stack.Screen name="listing/[id]" />
        <Stack.Screen name="alerts/[id]/results" />
        <Stack.Screen name="sources/index" />
        <Stack.Screen name="sources/add" />
        <Stack.Screen name="sources/catalog" />
        <Stack.Screen name="sources/[id]/index" />
        <Stack.Screen name="sources/[id]/edit" />
        <Stack.Screen name="settings/runtime" />
        <Stack.Screen name="+not-found" />
      </Stack>
    </>
  );
}
