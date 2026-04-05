import * as Notifications from 'expo-notifications';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: false,
    shouldSetBadge: false,
    shouldShowBanner: true,
    shouldShowList: true,
  }),
});

export const NotificationService = {
  async requestPermissions() {
    const existing = await Notifications.getPermissionsAsync();
    if (existing.granted) return { granted: true, platform: 'expo-notifications' };
    const requested = await Notifications.requestPermissionsAsync();
    return { granted: requested.granted, platform: 'expo-notifications' };
  },
  async notify(title: string, body: string) {
    const permissions = await Notifications.getPermissionsAsync();
    if (!permissions.granted) {
      return { queued: false, title, body, reason: 'permissions_not_granted' };
    }
    const id = await Notifications.scheduleNotificationAsync({
      content: { title, body },
      trigger: null,
    });
    return { queued: true, title, body, id };
  },
};
