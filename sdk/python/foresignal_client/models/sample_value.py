from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sample_status import SampleStatus
from ..models.sample_value_coverage import SampleValueCoverage
from ..models.sample_value_method import SampleValueMethod
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.sample_value_derived_type_0 import SampleValueDerivedType0


T = TypeVar("T", bound="SampleValue")


@_attrs_define
class SampleValue:
    """One product value at a point with validity, age, run and status

    Attributes:
        product_id (str):
        status (SampleStatus): ok; no-coverage: outside the grid/footprint or a no-coverage code ("outside product
            coverage; this is not evidence of clear weather"); no-data: inside coverage but nodata; stale: frame older than
            maxGap relative to the requested time; unavailable: product disabled, source failing, no frame or frame expired
        attribution (Attribution): Structured attribution clients must render for a product or derived output (Weather-
            Imagery-Design §6.10)
        value (float | None | Unset): Physical value (null for u/v products and non-ok statuses)
        u (float | None | Unset):
        v (float | None | Unset):
        derived (None | SampleValueDerivedType0 | Unset):
        units (None | str | Unset):
        label (None | str | Unset): Human label, e.g. category or `52.5 dBZ`
        frame_key (None | str | Unset):
        valid_time (datetime.datetime | None | Unset):
        local_time (None | str | Unset): Valid time in the place's timezone (else UTC), ISO-8601 with offset
        age_minutes (float | None | Unset):
        run (None | str | Unset):
        step (int | None | Unset):
        method (SampleValueMethod | Unset):
        coverage (SampleValueCoverage | Unset):
        status_text (None | str | Unset): Always present for non-ok statuses
    """

    product_id: str
    status: SampleStatus
    attribution: Attribution
    value: float | None | Unset = UNSET
    u: float | None | Unset = UNSET
    v: float | None | Unset = UNSET
    derived: None | SampleValueDerivedType0 | Unset = UNSET
    units: None | str | Unset = UNSET
    label: None | str | Unset = UNSET
    frame_key: None | str | Unset = UNSET
    valid_time: datetime.datetime | None | Unset = UNSET
    local_time: None | str | Unset = UNSET
    age_minutes: float | None | Unset = UNSET
    run: None | str | Unset = UNSET
    step: int | None | Unset = UNSET
    method: SampleValueMethod | Unset = UNSET
    coverage: SampleValueCoverage | Unset = UNSET
    status_text: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.sample_value_derived_type_0 import SampleValueDerivedType0

        product_id = self.product_id

        status = self.status.value

        attribution = self.attribution.to_dict()

        value: float | None | Unset
        if isinstance(self.value, Unset):
            value = UNSET
        else:
            value = self.value

        u: float | None | Unset
        if isinstance(self.u, Unset):
            u = UNSET
        else:
            u = self.u

        v: float | None | Unset
        if isinstance(self.v, Unset):
            v = UNSET
        else:
            v = self.v

        derived: dict[str, Any] | None | Unset
        if isinstance(self.derived, Unset):
            derived = UNSET
        elif isinstance(self.derived, SampleValueDerivedType0):
            derived = self.derived.to_dict()
        else:
            derived = self.derived

        units: None | str | Unset
        if isinstance(self.units, Unset):
            units = UNSET
        else:
            units = self.units

        label: None | str | Unset
        if isinstance(self.label, Unset):
            label = UNSET
        else:
            label = self.label

        frame_key: None | str | Unset
        if isinstance(self.frame_key, Unset):
            frame_key = UNSET
        else:
            frame_key = self.frame_key

        valid_time: None | str | Unset
        if isinstance(self.valid_time, Unset):
            valid_time = UNSET
        elif isinstance(self.valid_time, datetime.datetime):
            valid_time = self.valid_time.isoformat()
        else:
            valid_time = self.valid_time

        local_time: None | str | Unset
        if isinstance(self.local_time, Unset):
            local_time = UNSET
        else:
            local_time = self.local_time

        age_minutes: float | None | Unset
        if isinstance(self.age_minutes, Unset):
            age_minutes = UNSET
        else:
            age_minutes = self.age_minutes

        run: None | str | Unset
        if isinstance(self.run, Unset):
            run = UNSET
        else:
            run = self.run

        step: int | None | Unset
        if isinstance(self.step, Unset):
            step = UNSET
        else:
            step = self.step

        method: str | Unset = UNSET
        if not isinstance(self.method, Unset):
            method = self.method.value

        coverage: str | Unset = UNSET
        if not isinstance(self.coverage, Unset):
            coverage = self.coverage.value

        status_text: None | str | Unset
        if isinstance(self.status_text, Unset):
            status_text = UNSET
        else:
            status_text = self.status_text

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "productId": product_id,
                "status": status,
                "attribution": attribution,
            }
        )
        if value is not UNSET:
            field_dict["value"] = value
        if u is not UNSET:
            field_dict["u"] = u
        if v is not UNSET:
            field_dict["v"] = v
        if derived is not UNSET:
            field_dict["derived"] = derived
        if units is not UNSET:
            field_dict["units"] = units
        if label is not UNSET:
            field_dict["label"] = label
        if frame_key is not UNSET:
            field_dict["frameKey"] = frame_key
        if valid_time is not UNSET:
            field_dict["validTime"] = valid_time
        if local_time is not UNSET:
            field_dict["localTime"] = local_time
        if age_minutes is not UNSET:
            field_dict["ageMinutes"] = age_minutes
        if run is not UNSET:
            field_dict["run"] = run
        if step is not UNSET:
            field_dict["step"] = step
        if method is not UNSET:
            field_dict["method"] = method
        if coverage is not UNSET:
            field_dict["coverage"] = coverage
        if status_text is not UNSET:
            field_dict["statusText"] = status_text

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.sample_value_derived_type_0 import SampleValueDerivedType0

        d = dict(src_dict)
        product_id = d.pop("productId")

        status = SampleStatus(d.pop("status"))

        attribution = Attribution.from_dict(d.pop("attribution"))

        def _parse_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        value = _parse_value(d.pop("value", UNSET))

        def _parse_u(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        u = _parse_u(d.pop("u", UNSET))

        def _parse_v(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        v = _parse_v(d.pop("v", UNSET))

        def _parse_derived(data: object) -> None | SampleValueDerivedType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                derived_type_0 = SampleValueDerivedType0.from_dict(data)

                return derived_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SampleValueDerivedType0 | Unset, data)

        derived = _parse_derived(d.pop("derived", UNSET))

        def _parse_units(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        units = _parse_units(d.pop("units", UNSET))

        def _parse_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        label = _parse_label(d.pop("label", UNSET))

        def _parse_frame_key(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        frame_key = _parse_frame_key(d.pop("frameKey", UNSET))

        def _parse_valid_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                valid_time_type_0 = datetime.datetime.fromisoformat(data)

                return valid_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        valid_time = _parse_valid_time(d.pop("validTime", UNSET))

        def _parse_local_time(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        local_time = _parse_local_time(d.pop("localTime", UNSET))

        def _parse_age_minutes(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        age_minutes = _parse_age_minutes(d.pop("ageMinutes", UNSET))

        def _parse_run(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        run = _parse_run(d.pop("run", UNSET))

        def _parse_step(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        step = _parse_step(d.pop("step", UNSET))

        _method = d.pop("method", UNSET)
        method: SampleValueMethod | Unset
        if isinstance(_method, Unset):
            method = UNSET
        else:
            method = SampleValueMethod(_method)

        _coverage = d.pop("coverage", UNSET)
        coverage: SampleValueCoverage | Unset
        if isinstance(_coverage, Unset):
            coverage = UNSET
        else:
            coverage = SampleValueCoverage(_coverage)

        def _parse_status_text(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        status_text = _parse_status_text(d.pop("statusText", UNSET))

        sample_value = cls(
            product_id=product_id,
            status=status,
            attribution=attribution,
            value=value,
            u=u,
            v=v,
            derived=derived,
            units=units,
            label=label,
            frame_key=frame_key,
            valid_time=valid_time,
            local_time=local_time,
            age_minutes=age_minutes,
            run=run,
            step=step,
            method=method,
            coverage=coverage,
            status_text=status_text,
        )

        sample_value.additional_properties = d
        return sample_value

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Any:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Any) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
