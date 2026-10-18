from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.series_point_status import SeriesPointStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.series_point_extra import SeriesPointExtra


T = TypeVar("T", bound="SeriesPoint")


@_attrs_define
class SeriesPoint:
    """One data point of a series (`series-point` record payload, current version) (093)

    Attributes:
        period (str): Upstream period label Example: 2026-10-09T14.
        period_start (datetime.datetime):
        period_end (datetime.datetime):
        value (float | None):
        unit (str):
        status (SeriesPointStatus):
        extra (SeriesPointExtra | Unset): Flat scalar extras (e.g. `gasInStorageTWh`, `sharePct`)
        version (int | Unset): Record version; > 1 means the point was revised
        observed_at (datetime.datetime | Unset):
        known_at (datetime.datetime | Unset): Ingest time of the version that supplied the point (via `seq_clock`);
            rollups use bucket close time, rule samples their write time
        coverage_ok (bool | Unset): False when the involved sources were failing or not covering the period
    """

    period: str
    period_start: datetime.datetime
    period_end: datetime.datetime
    value: float | None
    unit: str
    status: SeriesPointStatus
    extra: SeriesPointExtra | Unset = UNSET
    version: int | Unset = UNSET
    observed_at: datetime.datetime | Unset = UNSET
    known_at: datetime.datetime | Unset = UNSET
    coverage_ok: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        period = self.period

        period_start = self.period_start.isoformat()

        period_end = self.period_end.isoformat()

        value: float | None
        value = self.value

        unit = self.unit

        status = self.status.value

        extra: dict[str, Any] | Unset = UNSET
        if not isinstance(self.extra, Unset):
            extra = self.extra.to_dict()

        version = self.version

        observed_at: str | Unset = UNSET
        if not isinstance(self.observed_at, Unset):
            observed_at = self.observed_at.isoformat()

        known_at: str | Unset = UNSET
        if not isinstance(self.known_at, Unset):
            known_at = self.known_at.isoformat()

        coverage_ok = self.coverage_ok

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "period": period,
                "periodStart": period_start,
                "periodEnd": period_end,
                "value": value,
                "unit": unit,
                "status": status,
            }
        )
        if extra is not UNSET:
            field_dict["extra"] = extra
        if version is not UNSET:
            field_dict["version"] = version
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if known_at is not UNSET:
            field_dict["knownAt"] = known_at
        if coverage_ok is not UNSET:
            field_dict["coverageOk"] = coverage_ok

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.series_point_extra import SeriesPointExtra

        d = dict(src_dict)
        period = d.pop("period")

        period_start = datetime.datetime.fromisoformat(d.pop("periodStart"))

        period_end = datetime.datetime.fromisoformat(d.pop("periodEnd"))

        def _parse_value(data: object) -> float | None:
            if data is None:
                return data
            return cast(float | None, data)

        value = _parse_value(d.pop("value"))

        unit = d.pop("unit")

        status = SeriesPointStatus(d.pop("status"))

        _extra = d.pop("extra", UNSET)
        extra: SeriesPointExtra | Unset
        if isinstance(_extra, Unset):
            extra = UNSET
        else:
            extra = SeriesPointExtra.from_dict(_extra)

        version = d.pop("version", UNSET)

        _observed_at = d.pop("observedAt", UNSET)
        observed_at: datetime.datetime | Unset
        if isinstance(_observed_at, Unset):
            observed_at = UNSET
        else:
            observed_at = datetime.datetime.fromisoformat(_observed_at)

        _known_at = d.pop("knownAt", UNSET)
        known_at: datetime.datetime | Unset
        if isinstance(_known_at, Unset):
            known_at = UNSET
        else:
            known_at = datetime.datetime.fromisoformat(_known_at)

        coverage_ok = d.pop("coverageOk", UNSET)

        series_point = cls(
            period=period,
            period_start=period_start,
            period_end=period_end,
            value=value,
            unit=unit,
            status=status,
            extra=extra,
            version=version,
            observed_at=observed_at,
            known_at=known_at,
            coverage_ok=coverage_ok,
        )

        series_point.additional_properties = d
        return series_point

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
