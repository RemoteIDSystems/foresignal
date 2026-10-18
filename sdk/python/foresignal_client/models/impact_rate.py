from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.impact_rate_source import ImpactRateSource
from ..models.impact_rate_status import ImpactRateStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.impact_rate_interval_type_0 import ImpactRateIntervalType0


T = TypeVar("T", bound="ImpactRate")


@_attrs_define
class ImpactRate:
    """Historical conditional rate P(impact | hazard class) from 121 base-rate machinery

    Attributes:
        hazard_class (str):  Example: tropical-wind-34.
        impact_family (str):  Example: outage.
        region_class (str):  Example: admin1:coastal.
        n (int): Hazard occurrences
        status (ImpactRateStatus): `insufficient` when n < 20 (pair skipped)
        computed_at (datetime.datetime):
        lag_hours (int | Unset):
        rate (float | None | Unset):
        interval (ImpactRateIntervalType0 | None | Unset):
        n_impacts (int | None | Unset):
        source (ImpactRateSource | Unset):
        base_rate_id (None | str | Unset):
    """

    hazard_class: str
    impact_family: str
    region_class: str
    n: int
    status: ImpactRateStatus
    computed_at: datetime.datetime
    lag_hours: int | Unset = UNSET
    rate: float | None | Unset = UNSET
    interval: ImpactRateIntervalType0 | None | Unset = UNSET
    n_impacts: int | None | Unset = UNSET
    source: ImpactRateSource | Unset = UNSET
    base_rate_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.impact_rate_interval_type_0 import ImpactRateIntervalType0

        hazard_class = self.hazard_class

        impact_family = self.impact_family

        region_class = self.region_class

        n = self.n

        status = self.status.value

        computed_at = self.computed_at.isoformat()

        lag_hours = self.lag_hours

        rate: float | None | Unset
        if isinstance(self.rate, Unset):
            rate = UNSET
        else:
            rate = self.rate

        interval: dict[str, Any] | None | Unset
        if isinstance(self.interval, Unset):
            interval = UNSET
        elif isinstance(self.interval, ImpactRateIntervalType0):
            interval = self.interval.to_dict()
        else:
            interval = self.interval

        n_impacts: int | None | Unset
        if isinstance(self.n_impacts, Unset):
            n_impacts = UNSET
        else:
            n_impacts = self.n_impacts

        source: str | Unset = UNSET
        if not isinstance(self.source, Unset):
            source = self.source.value

        base_rate_id: None | str | Unset
        if isinstance(self.base_rate_id, Unset):
            base_rate_id = UNSET
        else:
            base_rate_id = self.base_rate_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "hazardClass": hazard_class,
                "impactFamily": impact_family,
                "regionClass": region_class,
                "n": n,
                "status": status,
                "computedAt": computed_at,
            }
        )
        if lag_hours is not UNSET:
            field_dict["lagHours"] = lag_hours
        if rate is not UNSET:
            field_dict["rate"] = rate
        if interval is not UNSET:
            field_dict["interval"] = interval
        if n_impacts is not UNSET:
            field_dict["nImpacts"] = n_impacts
        if source is not UNSET:
            field_dict["source"] = source
        if base_rate_id is not UNSET:
            field_dict["baseRateId"] = base_rate_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.impact_rate_interval_type_0 import ImpactRateIntervalType0

        d = dict(src_dict)
        hazard_class = d.pop("hazardClass")

        impact_family = d.pop("impactFamily")

        region_class = d.pop("regionClass")

        n = d.pop("n")

        status = ImpactRateStatus(d.pop("status"))

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        lag_hours = d.pop("lagHours", UNSET)

        def _parse_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        rate = _parse_rate(d.pop("rate", UNSET))

        def _parse_interval(data: object) -> ImpactRateIntervalType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                interval_type_0 = ImpactRateIntervalType0.from_dict(data)

                return interval_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ImpactRateIntervalType0 | None | Unset, data)

        interval = _parse_interval(d.pop("interval", UNSET))

        def _parse_n_impacts(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        n_impacts = _parse_n_impacts(d.pop("nImpacts", UNSET))

        _source = d.pop("source", UNSET)
        source: ImpactRateSource | Unset
        if isinstance(_source, Unset):
            source = UNSET
        else:
            source = ImpactRateSource(_source)

        def _parse_base_rate_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        base_rate_id = _parse_base_rate_id(d.pop("baseRateId", UNSET))

        impact_rate = cls(
            hazard_class=hazard_class,
            impact_family=impact_family,
            region_class=region_class,
            n=n,
            status=status,
            computed_at=computed_at,
            lag_hours=lag_hours,
            rate=rate,
            interval=interval,
            n_impacts=n_impacts,
            source=source,
            base_rate_id=base_rate_id,
        )

        impact_rate.additional_properties = d
        return impact_rate

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
