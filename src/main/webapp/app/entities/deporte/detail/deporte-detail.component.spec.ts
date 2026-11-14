import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { DeporteDetailComponent } from './deporte-detail.component';

describe('Deporte Management Detail Component', () => {
  let comp: DeporteDetailComponent;
  let fixture: ComponentFixture<DeporteDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeporteDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./deporte-detail.component').then(m => m.DeporteDetailComponent),
              resolve: { deporte: () => of({ id: 28849 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(DeporteDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DeporteDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load deporte on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', DeporteDetailComponent);

      // THEN
      expect(instance.deporte()).toEqual(expect.objectContaining({ id: 28849 }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
