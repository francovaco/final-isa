import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IDeporte, NewDeporte } from '../deporte.model';

export type PartialUpdateDeporte = Partial<IDeporte> & Pick<IDeporte, 'id'>;

export type EntityResponseType = HttpResponse<IDeporte>;
export type EntityArrayResponseType = HttpResponse<IDeporte[]>;

@Injectable({ providedIn: 'root' })
export class DeporteService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/deportes');

  create(deporte: NewDeporte): Observable<EntityResponseType> {
    return this.http.post<IDeporte>(this.resourceUrl, deporte, { observe: 'response' });
  }

  update(deporte: IDeporte): Observable<EntityResponseType> {
    return this.http.put<IDeporte>(`${this.resourceUrl}/${this.getDeporteIdentifier(deporte)}`, deporte, { observe: 'response' });
  }

  partialUpdate(deporte: PartialUpdateDeporte): Observable<EntityResponseType> {
    return this.http.patch<IDeporte>(`${this.resourceUrl}/${this.getDeporteIdentifier(deporte)}`, deporte, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IDeporte>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IDeporte[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getDeporteIdentifier(deporte: Pick<IDeporte, 'id'>): number {
    return deporte.id;
  }

  compareDeporte(o1: Pick<IDeporte, 'id'> | null, o2: Pick<IDeporte, 'id'> | null): boolean {
    return o1 && o2 ? this.getDeporteIdentifier(o1) === this.getDeporteIdentifier(o2) : o1 === o2;
  }

  addDeporteToCollectionIfMissing<Type extends Pick<IDeporte, 'id'>>(
    deporteCollection: Type[],
    ...deportesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const deportes: Type[] = deportesToCheck.filter(isPresent);
    if (deportes.length > 0) {
      const deporteCollectionIdentifiers = deporteCollection.map(deporteItem => this.getDeporteIdentifier(deporteItem));
      const deportesToAdd = deportes.filter(deporteItem => {
        const deporteIdentifier = this.getDeporteIdentifier(deporteItem);
        if (deporteCollectionIdentifiers.includes(deporteIdentifier)) {
          return false;
        }
        deporteCollectionIdentifiers.push(deporteIdentifier);
        return true;
      });
      return [...deportesToAdd, ...deporteCollection];
    }
    return deporteCollection;
  }
}
