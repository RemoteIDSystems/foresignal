from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sample_status import SampleStatus
from ..models.series_sample_kind import SeriesSampleKind
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.series_sample_derived_type_0 import SeriesSampleDerivedType0


T = TypeVar("T", bound="SeriesSample")


@_attrs_define
class SeriesSample:
    """One step of a point series

    Attributes:
        time (datetime.datetime):
        kind (SeriesSampleKind):
        status (SampleStatus): ok; no-coverage: outside the grid/footprint or a no-coverage code ("outside product
            coverage; this is not evidence of clear weather"); no-data: inside coverage but nodata; stale: frame older than
            maxGap relative to the requested time; unavailable: product disabled, source failing, no frame or frame expired
        value (float | None | Unset):
        u (float | None | Unset):
        v (float | None | Unset):
        derived (None | SeriesSampleDerivedType0 | Unset):
        frame_key (None | str | Unset):
        run (None | str | Unset):
        step (int | None | Unset):
        status_text (None | str | Unset):
    """

    time: datetime.datetime
    kind: SeriesSampleKind
    status: SampleStatus
    value: float | None | Unset = UNSET
    u: float | None | Unset = UNSET
    v: float | None | Unset = UNSET
    derived: None | SeriesSampleDerivedType0 | Unset = UNSET
    frame_key: None | str | Unset = UNSET
    run: None | str | Unset = UNSET
    step: int | None | Unset = UNSET
    status_text: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.series_sample_derived_type_0 import SeriesSampleDerivedType0

        time = self.time.isoformat()

        kind = self.kind.value

        status = self.status.value

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
        elif isinstance(self.derived, SeriesSampleDerivedType0):
            derived = self.derived.to_dict()
        else:
            derived = self.derived

        frame_key: None | str | Unset
        if isinstance(self.frame_key, Unset):
            frame_key = UNSET
        else:
            frame_key = self.frame_key

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

        status_text: None | str | Unset
        if isinstance(self.status_text, Unset):
            status_text = UNSET
        else:
            status_text = self.status_text

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "time": time,
                "kind": kind,
                "status": status,
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
        if frame_key is not UNSET:
            field_dict["frameKey"] = frame_key
        if run is not UNSET:
            field_dict["run"] = run
        if step is not UNSET:
            field_dict["step"] = step
        if status_text is not UNSET:
            field_dict["statusText"] = status_text

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.series_sample_derived_type_0 import SeriesSampleDerivedType0

        d = dict(src_dict)
        time = datetime.datetime.fromisoformat(d.pop("time"))

        kind = SeriesSampleKind(d.pop("kind"))

        status = SampleStatus(d.pop("status"))

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

        def _parse_derived(data: object) -> None | SeriesSampleDerivedType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                derived_type_0 = SeriesSampleDerivedType0.from_dict(data)

                return derived_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SeriesSampleDerivedType0 | Unset, data)

        derived = _parse_derived(d.pop("derived", UNSET))

        def _parse_frame_key(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        frame_key = _parse_frame_key(d.pop("frameKey", UNSET))

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

        def _parse_status_text(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        status_text = _parse_status_text(d.pop("statusText", UNSET))

        series_sample = cls(
            time=time,
            kind=kind,
            status=status,
            value=value,
            u=u,
            v=v,
            derived=derived,
            frame_key=frame_key,
            run=run,
            step=step,
            status_text=status_text,
        )

        series_sample.additional_properties = d
        return series_sample

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
