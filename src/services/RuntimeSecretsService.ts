import * as SecureStore from 'expo-secure-store';

const KEY = 'homesync-worker-token';

export const RuntimeSecretsService = {
  async getWorkerToken() {
    return SecureStore.getItemAsync(KEY);
  },
  async setWorkerToken(token: string | null) {
    if (!token) {
      await SecureStore.deleteItemAsync(KEY);
      return;
    }
    await SecureStore.setItemAsync(KEY, token);
  },
};
