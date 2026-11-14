import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { DeporteService } from '../service/deporte.service';
import { IDeporte } from '../deporte.model';
import { DeporteFormService } from './deporte-form.service';

import { DeporteUpdateComponent } from './deporte-update.component';

describe('Deporte Management Update Component', () => {
  let comp: DeporteUpdateComponent;
  let fixture: ComponentFixture<DeporteUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let deporteFormService: DeporteFormService;
  let deporteService: DeporteService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [DeporteUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(DeporteUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(DeporteUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    deporteFormService = TestBed.inject(DeporteFormService);
    deporteService = TestBed.inject(DeporteService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const deporte: IDeporte = { id: 25881 };

      activatedRoute.data = of({ deporte });
      comp.ngOnInit();

      expect(comp.deporte).toEqual(deporte);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IDeporte>>();
      const deporte = { id: 28849 };
      jest.spyOn(deporteFormService, 'getDeporte').mockReturnValue(deporte);
      jest.spyOn(deporteService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ deporte });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: deporte }));
      saveSubject.complete();

      // THEN
      expect(deporteFormService.getDeporte).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(deporteService.update).toHaveBeenCalledWith(expect.objectContaining(deporte));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IDeporte>>();
      const deporte = { id: 28849 };
      jest.spyOn(deporteFormService, 'getDeporte').mockReturnValue({ id: null });
      jest.spyOn(deporteService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ deporte: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: deporte }));
      saveSubject.complete();

      // THEN
      expect(deporteFormService.getDeporte).toHaveBeenCalled();
      expect(deporteService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IDeporte>>();
      const deporte = { id: 28849 };
      jest.spyOn(deporteService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ deporte });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(deporteService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
