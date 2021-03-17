import { WebPlugin } from '@capacitor/core';
import { AppUpdatePlugin, UpdateRequestOptions } from './definitions';
export declare class AppUpdateWeb extends WebPlugin implements AppUpdatePlugin {
    constructor();
    runAutoUpdate(options: UpdateRequestOptions): Promise<void>;
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
}
declare const AppUpdate: AppUpdateWeb;
export { AppUpdate };
