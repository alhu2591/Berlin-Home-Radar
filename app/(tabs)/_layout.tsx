import { Tabs } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { tokens } from '@/design/tokens';

export default function TabLayout() {
  return (
    <Tabs
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: tokens.colors.primary,
        tabBarInactiveTintColor: tokens.colors.textMuted,
        tabBarStyle: { borderTopColor: tokens.colors.border, height: 64, paddingBottom: 8 },
        tabBarIcon: ({ color, size }) => {
          const map = {
            index: 'home-outline',
            search: 'search-outline',
            saved: 'heart-outline',
            alerts: 'notifications-outline',
            settings: 'settings-outline',
          } as const;
          return <Ionicons name={map[route.name as keyof typeof map] || 'ellipse-outline'} size={size} color={color} />;
        },
      })}
    >
      <Tabs.Screen name="index" options={{ title: 'Discover' }} />
      <Tabs.Screen name="search" options={{ title: 'Search' }} />
      <Tabs.Screen name="saved" options={{ title: 'Saved' }} />
      <Tabs.Screen name="alerts" options={{ title: 'Alerts' }} />
      <Tabs.Screen name="settings" options={{ title: 'Settings' }} />
    </Tabs>
  );
}
