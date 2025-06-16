import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { PlayerListComponent } from './playerlist/playerlist';
import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { provideHttpClient,withFetch} from '@angular/common/http';
import { TeamsComponent } from './teamlist/teamlist';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter([
      { path: 'players', component: PlayerListComponent },
      {path:'teams',component: TeamsComponent}
    ]),
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes), provideClientHydration(withEventReplay()),provideHttpClient(withFetch()),
  ]
};
