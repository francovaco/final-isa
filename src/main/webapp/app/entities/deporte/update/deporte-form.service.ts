import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IDeporte, NewDeporte } from '../deporte.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IDeporte for edit and NewDeporteFormGroupInput for create.
 */
type DeporteFormGroupInput = IDeporte | PartialWithRequiredKeyOf<NewDeporte>;

type DeporteFormDefaults = Pick<NewDeporte, 'id'>;

type DeporteFormGroupContent = {
  id: FormControl<IDeporte['id'] | NewDeporte['id']>;
  nombre: FormControl<IDeporte['nombre']>;
  descripcion: FormControl<IDeporte['descripcion']>;
  jugadoresPorEquipo: FormControl<IDeporte['jugadoresPorEquipo']>;
};

export type DeporteFormGroup = FormGroup<DeporteFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class DeporteFormService {
  createDeporteFormGroup(deporte: DeporteFormGroupInput = { id: null }): DeporteFormGroup {
    const deporteRawValue = {
      ...this.getFormDefaults(),
      ...deporte,
    };
    return new FormGroup<DeporteFormGroupContent>({
      id: new FormControl(
        { value: deporteRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      nombre: new FormControl(deporteRawValue.nombre, {
        validators: [Validators.required, Validators.maxLength(40)],
      }),
      descripcion: new FormControl(deporteRawValue.descripcion, {
        validators: [Validators.maxLength(255)],
      }),
      jugadoresPorEquipo: new FormControl(deporteRawValue.jugadoresPorEquipo, {
        validators: [Validators.min(1), Validators.max(11)],
      }),
    });
  }

  getDeporte(form: DeporteFormGroup): IDeporte | NewDeporte {
    return form.getRawValue() as IDeporte | NewDeporte;
  }

  resetForm(form: DeporteFormGroup, deporte: DeporteFormGroupInput): void {
    const deporteRawValue = { ...this.getFormDefaults(), ...deporte };
    form.reset(
      {
        ...deporteRawValue,
        id: { value: deporteRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): DeporteFormDefaults {
    return {
      id: null,
    };
  }
}
