import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ICancha, NewCancha } from '../cancha.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ICancha for edit and NewCanchaFormGroupInput for create.
 */
type CanchaFormGroupInput = ICancha | PartialWithRequiredKeyOf<NewCancha>;

type CanchaFormDefaults = Pick<NewCancha, 'id' | 'techada' | 'activa'>;

type CanchaFormGroupContent = {
  id: FormControl<ICancha['id'] | NewCancha['id']>;
  nombre: FormControl<ICancha['nombre']>;
  superficie: FormControl<ICancha['superficie']>;
  techada: FormControl<ICancha['techada']>;
  precioPorHora: FormControl<ICancha['precioPorHora']>;
  activa: FormControl<ICancha['activa']>;
  deporte: FormControl<ICancha['deporte']>;
};

export type CanchaFormGroup = FormGroup<CanchaFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class CanchaFormService {
  createCanchaFormGroup(cancha: CanchaFormGroupInput = { id: null }): CanchaFormGroup {
    const canchaRawValue = {
      ...this.getFormDefaults(),
      ...cancha,
    };
    return new FormGroup<CanchaFormGroupContent>({
      id: new FormControl(
        { value: canchaRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      nombre: new FormControl(canchaRawValue.nombre, {
        validators: [Validators.required, Validators.maxLength(40)],
      }),
      superficie: new FormControl(canchaRawValue.superficie, {
        validators: [Validators.required],
      }),
      techada: new FormControl(canchaRawValue.techada, {
        validators: [Validators.required],
      }),
      precioPorHora: new FormControl(canchaRawValue.precioPorHora, {
        validators: [Validators.required, Validators.min(0)],
      }),
      activa: new FormControl(canchaRawValue.activa, {
        validators: [Validators.required],
      }),
      deporte: new FormControl(canchaRawValue.deporte, {
        validators: [Validators.required],
      }),
    });
  }

  getCancha(form: CanchaFormGroup): ICancha | NewCancha {
    return form.getRawValue() as ICancha | NewCancha;
  }

  resetForm(form: CanchaFormGroup, cancha: CanchaFormGroupInput): void {
    const canchaRawValue = { ...this.getFormDefaults(), ...cancha };
    form.reset(
      {
        ...canchaRawValue,
        id: { value: canchaRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): CanchaFormDefaults {
    return {
      id: null,
      techada: false,
      activa: false,
    };
  }
}
