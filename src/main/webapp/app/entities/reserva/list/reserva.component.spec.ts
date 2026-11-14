import { ComponentFixture, TestBed, fakeAsync, inject, tick } from '@angular/core/testing';
import { HttpHeaders, HttpResponse, provideHttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Subject, of } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import dayjs from 'dayjs/esm';

import { AccountService } from 'app/core/auth/account.service';
import { sampleWithRequiredData } from '../reserva.test-samples';
import { ReservaService } from '../service/reserva.service';

import { ReservaComponent } from './reserva.component';
import SpyInstance = jest.SpyInstance;

describe('Reserva Management Component', () => {
  let comp: ReservaComponent;
  let fixture: ComponentFixture<ReservaComponent>;
  let service: ReservaService;
  let routerNavigateSpy: SpyInstance<Promise<boolean>>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ReservaComponent],
      providers: [
        provideHttpClient(),
        {
          provide: ActivatedRoute,
          useValue: {
            data: of({
              defaultSort: 'id,asc',
            }),
            queryParamMap: of(
              jest.requireActual('@angular/router').convertToParamMap({
                page: '1',
                size: '1',
                sort: 'id,desc',
                'filter[someId.in]': 'dc4279ea-cfb9-11ec-9d64-0242ac120002',
              }),
            ),
            snapshot: {
              queryParams: {},
              queryParamMap: jest.requireActual('@angular/router').convertToParamMap({
                page: '1',
                size: '1',
                sort: 'id,desc',
                'filter[someId.in]': 'dc4279ea-cfb9-11ec-9d64-0242ac120002',
              }),
            },
          },
        },
      ],
    })
      .overrideTemplate(ReservaComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ReservaComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(ReservaService);
    routerNavigateSpy = jest.spyOn(comp.router, 'navigate');

    jest
      .spyOn(service, 'query')
      .mockReturnValueOnce(
        of(
          new HttpResponse({
            body: [{ id: 15097 }],
            headers: new HttpHeaders({
              link: '<http://localhost/api/foo?page=1&size=20>; rel="next"',
            }),
          }),
        ),
      )
      .mockReturnValueOnce(
        of(
          new HttpResponse({
            body: [{ id: 8835 }],
            headers: new HttpHeaders({
              link: '<http://localhost/api/foo?page=0&size=20>; rel="prev",<http://localhost/api/foo?page=2&size=20>; rel="next"',
            }),
          }),
        ),
      );
  });

  it('should call load all on init', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenCalled();
    expect(comp.reservas()[0]).toEqual(expect.objectContaining({ id: 15097 }));
  });

  describe('trackId', () => {
    it('should forward to reservaService', () => {
      const entity = { id: 15097 };
      jest.spyOn(service, 'getReservaIdentifier');
      const id = comp.trackId(entity);
      expect(service.getReservaIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });

  it('should calculate the sort attribute for a non-id attribute', () => {
    // WHEN
    comp.navigateToWithComponentValues({ predicate: 'non-existing-column', order: 'asc' });

    // THEN
    expect(routerNavigateSpy).toHaveBeenLastCalledWith(
      expect.anything(),
      expect.objectContaining({
        queryParams: expect.objectContaining({
          sort: ['non-existing-column,asc'],
        }),
      }),
    );
  });

  it('should load a page', () => {
    // WHEN
    comp.navigateToPage(1);

    // THEN
    expect(routerNavigateSpy).toHaveBeenCalled();
  });

  it('should calculate the sort attribute for an id', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenLastCalledWith(expect.objectContaining({ sort: ['id,desc'] }));
  });

  it('should calculate the filter attribute', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenLastCalledWith(expect.objectContaining({ 'someId.in': ['dc4279ea-cfb9-11ec-9d64-0242ac120002'] }));
  });

  describe('delete', () => {
    let ngbModal: NgbModal;
    let deleteModalMock: any;

    beforeEach(() => {
      deleteModalMock = { componentInstance: {}, closed: new Subject() };
      // NgbModal is not a singleton using TestBed.inject.
      // ngbModal = TestBed.inject(NgbModal);
      ngbModal = (comp as any).modalService;
      jest.spyOn(ngbModal, 'open').mockReturnValue(deleteModalMock);
    });

    it('on confirm should call load', inject(
      [],
      fakeAsync(() => {
        // GIVEN
        jest.spyOn(comp, 'load');

        // WHEN
        comp.delete(sampleWithRequiredData);
        deleteModalMock.closed.next('deleted');
        tick();

        // THEN
        expect(ngbModal.open).toHaveBeenCalled();
        expect(comp.load).toHaveBeenCalled();
      }),
    ));

    it('on dismiss should call load', inject(
      [],
      fakeAsync(() => {
        // GIVEN
        jest.spyOn(comp, 'load');

        // WHEN
        comp.delete(sampleWithRequiredData);
        deleteModalMock.closed.next();
        tick();

        // THEN
        expect(ngbModal.open).toHaveBeenCalled();
        expect(comp.load).not.toHaveBeenCalled();
      }),
    ));
  });
  describe('cancelar', () => {
    const reservaFutura = { id: 77, estado: 'CONFIRMADA' as const, inicio: dayjs().add(3, 'day') };

    it('pide confirmación y, si el usuario acepta, cancela y recarga la lista', () => {
      jest.spyOn(window, 'confirm').mockReturnValue(true);
      jest.spyOn(service, 'cancelar').mockReturnValue(of(new HttpResponse({ body: { ...reservaFutura, estado: 'CANCELADA' as const } })));
      jest.spyOn(comp, 'load').mockImplementation(() => undefined);

      comp.cancelar(reservaFutura);

      expect(service.cancelar).toHaveBeenCalledWith(77);
      expect(comp.load).toHaveBeenCalled();
    });

    it('no cancela si el usuario no confirma', () => {
      jest.spyOn(window, 'confirm').mockReturnValue(false);
      jest.spyOn(service, 'cancelar');

      comp.cancelar(reservaFutura);

      expect(service.cancelar).not.toHaveBeenCalled();
    });

    it('muestra el botón Cancelar según el rol y la anticipación', () => {
      const accountService = TestBed.inject(AccountService);
      const reservaEnUnaHora = { id: 78, estado: 'CONFIRMADA' as const, inicio: dayjs().add(1, 'hour') };

      jest.spyOn(accountService, 'hasAnyAuthority').mockReturnValue(false);
      expect(comp.puedeCancelar(reservaFutura)).toBe(true);
      expect(comp.puedeCancelar(reservaEnUnaHora)).toBe(false);

      jest.spyOn(accountService, 'hasAnyAuthority').mockReturnValue(true);
      expect(comp.puedeCancelar(reservaEnUnaHora)).toBe(true);
    });
  });
});
