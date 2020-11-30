import { WebPlugin } from '@capacitor/core';
import { AppUpdatePlugin, UpdateRequestOptions } from './definitions';

export class AppUpdateWeb extends WebPlugin implements AppUpdatePlugin {
  constructor() {
    super({
      name: 'AppUpdate',
      platforms: ['web'],
    });
  }

  runAutoUpdate(options: UpdateRequestOptions): Promise<void> {
    console.log(options);

    return Promise.resolve();
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}

const AppUpdate = new AppUpdateWeb();

export { AppUpdate };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(AppUpdate);
