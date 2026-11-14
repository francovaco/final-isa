import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { ICancha } from 'app/entities/cancha/cancha.model';
import { CanchaService } from 'app/entities/cancha/service/cancha.service';
import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { AccountService } from 'app/core/auth/account.service';
import { ClimaService, PronosticoDia } from 'app/shared/clima/clima.service';
import { IReserva } from '../reserva.model';
import { ReservaService } from '../service/reserva.service';
import { ReservaFormService } from './reserva-form.service';

import { ReservaUpdateComponent } from './reserva-update.component';

describe('Reserva Management Update Component', () => {
  let comp: ReservaUpdateComponent;
  let fixture: ComponentFixture<ReservaUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let reservaFormService: ReservaFormService;
  let reservaService: ReservaService;
  let canchaService: CanchaService;
  let userService: UserService;
  let accountService: AccountService;
  let climaService: ClimaService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ReservaUpdateComponent],
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
      .overrideTemplate(ReservaUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ReservaUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    reservaFormService = TestBed.inject(ReservaFormService);
    reservaService = TestBed.inject(ReservaService);
    canchaService = TestBed.inject(CanchaService);
    userService = TestBed.inject(UserService);
    accountService = TestBed.inject(AccountService);
    climaService = TestBed.inject(ClimaService);
    // Por defecto: usuario admin y sin pronóstico (no se llama a la API real)
    jest.spyOn(accountService, 'hasAnyAuthority').mockReturnValue(true);
    jest.spyOn(climaService, 'pronosticoDelDia').mockReturnValue(of(null));

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Cancha query and add missing value', () => {
      const reserva: IReserva = { id: 8835 };
      const cancha: ICancha = { id: 16283 };
      reserva.cancha = cancha;

      const canchaCollection: ICancha[] = [{ id: 16283, activa: true }];
      jest.spyOn(canchaService, 'query').mockReturnValue(of(new HttpResponse({ body: canchaCollection })));
      const additionalCanchas = [cancha];
      const expectedCollection: ICancha[] = [...additionalCanchas, ...canchaCollection];
      jest.spyOn(canchaService, 'addCanchaToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ reserva });
      comp.ngOnInit();

      expect(canchaService.query).toHaveBeenCalled();
      expect(canchaService.addCanchaToCollectionIfMissing).toHaveBeenCalledWith(
        canchaCollection,
        ...additionalCanchas.map(expect.objectContaining),
      );
      expect(comp.canchasSharedCollection).toEqual(expectedCollection);
    });

    it('should call User query and add missing value', () => {
      const reserva: IReserva = { id: 8835 };
      const usuario: IUser = { id: 3944 };
      reserva.usuario = usuario;

      const userCollection: IUser[] = [{ id: 3944 }];
      jest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [usuario];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      jest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ reserva });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(expect.objectContaining),
      );
      expect(comp.usersSharedCollection).toEqual(expectedCollection);
    });

    it('no ofrece canchas inactivas', () => {
      jest.spyOn(canchaService, 'query').mockReturnValue(
        of(
          new HttpResponse({
            body: [
              { id: 1, activa: true },
              { id: 2, activa: false },
              { id: 3, activa: true },
            ],
          }),
        ),
      );

      activatedRoute.data = of({ reserva: null });
      comp.ngOnInit();

      expect(comp.canchasSharedCollection.map(c => c.id)).toEqual([1, 3]);
    });

    it('un socio no carga la lista de usuarios', () => {
      jest.spyOn(accountService, 'hasAnyAuthority').mockReturnValue(false);
      jest.spyOn(userService, 'query');

      activatedRoute.data = of({ reserva: null });
      comp.ngOnInit();

      expect(userService.query).not.toHaveBeenCalled();
    });

    it('muestra el pronóstico del día elegido', fakeAsync(() => {
      const pronostico: PronosticoDia = {
        fecha: '2026-12-01',
        descripcion: 'Despejado',
        temperaturaMax: 31,
        temperaturaMin: 18,
        probabilidadLluvia: 5,
      };
      jest.spyOn(climaService, 'pronosticoDelDia').mockReturnValue(of(pronostico));
      jest.spyOn(accountService, 'hasAnyAuthority').mockReturnValue(false);
      jest.spyOn(canchaService, 'query').mockReturnValue(of(new HttpResponse({ body: [] })));

      activatedRoute.data = of({ reserva: null });
      comp.ngOnInit();
      comp.editForm.controls.inicio.setValue('2026-12-01T10:00');
      tick(300);

      expect(climaService.pronosticoDelDia).toHaveBeenLastCalledWith('2026-12-01');
      expect(comp.pronostico()).toEqual(pronostico);
    }));

    it('should update editForm', () => {
      const reserva: IReserva = { id: 8835 };
      const cancha: ICancha = { id: 16283 };
      reserva.cancha = cancha;
      const usuario: IUser = { id: 3944 };
      reserva.usuario = usuario;

      activatedRoute.data = of({ reserva });
      comp.ngOnInit();

      expect(comp.canchasSharedCollection).toContainEqual(cancha);
      expect(comp.usersSharedCollection).toContainEqual(usuario);
      expect(comp.reserva).toEqual(reserva);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IReserva>>();
      const reserva = { id: 15097 };
      jest.spyOn(reservaFormService, 'getReserva').mockReturnValue(reserva);
      jest.spyOn(reservaService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ reserva });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: reserva }));
      saveSubject.complete();

      // THEN
      expect(reservaFormService.getReserva).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(reservaService.update).toHaveBeenCalledWith(expect.objectContaining(reserva));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IReserva>>();
      const reserva = { id: 15097 };
      jest.spyOn(reservaFormService, 'getReserva').mockReturnValue({ id: null });
      jest.spyOn(reservaService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ reserva: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: reserva }));
      saveSubject.complete();

      // THEN
      expect(reservaFormService.getReserva).toHaveBeenCalled();
      expect(reservaService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IReserva>>();
      const reserva = { id: 15097 };
      jest.spyOn(reservaService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ reserva });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(reservaService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareCancha', () => {
      it('should forward to canchaService', () => {
        const entity = { id: 16283 };
        const entity2 = { id: 15070 };
        jest.spyOn(canchaService, 'compareCancha');
        comp.compareCancha(entity, entity2);
        expect(canchaService.compareCancha).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareUser', () => {
      it('should forward to userService', () => {
        const entity = { id: 3944 };
        const entity2 = { id: 6275 };
        jest.spyOn(userService, 'compareUser');
        comp.compareUser(entity, entity2);
        expect(userService.compareUser).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
