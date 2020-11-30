declare module '@capacitor/core' {
  interface PluginRegistry {
    AppUpdate: AppUpdatePlugin;
  }
}

export interface AuthenticationOptions {
  authType: string;
  username: string;
  password: string;
}

export interface UpdateRequestOptions {
  serverUrl: string;
  fileName: string;
  folderName?: string;
  authenticationOptions?: AuthenticationOptions;
}

export interface AppUpdatePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;

  runAutoUpdate(options: UpdateRequestOptions): Promise<void>;
}
