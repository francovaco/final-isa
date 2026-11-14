import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';

import { AccountService } from 'app/core/auth/account.service';
import { AuthServerProvider } from 'app/core/auth/auth-jwt.service';
import { PwaService } from 'app/core/pwa/pwa.service';
import { LoginService } from './login.service';

describe('LoginService', () => {
  it('al cerrar sesión borra los datos cacheados por el service worker', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient()] });
    const pwaService = TestBed.inject(PwaService);
    const authServerProvider = TestBed.inject(AuthServerProvider);
    const accountService = TestBed.inject(AccountService);
    jest.spyOn(pwaService, 'limpiarCacheDeDatos').mockResolvedValue();
    jest.spyOn(authServerProvider, 'logout').mockReturnValue(of(undefined));
    jest.spyOn(accountService, 'authenticate');

    TestBed.inject(LoginService).logout();

    expect(pwaService.limpiarCacheDeDatos).toHaveBeenCalled();
    expect(accountService.authenticate).toHaveBeenCalledWith(null);
  });
});
