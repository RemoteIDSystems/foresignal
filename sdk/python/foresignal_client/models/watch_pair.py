from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.event_selector import EventSelector


T = TypeVar("T", bound="WatchPair")


@_attrs_define
class WatchPair:
    """Event-study watch pair: qualifying events of a family checked against this series live

    Attributes:
        event_family (str): Signal family from the 119 registry (`intel/signal-families.json`, operator-extensible),
            e.g. `ais-behaviour`, `aviation-military`, `aviation-ops`, `conflict`, `outage`, `internet-measurement`, `gnss-
            interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`, `markets`, `sanctions`,
            `health`, `space`
        region_codes (list[str] | Unset): Event regions (empty = any)
        selector (EventSelector | None | Unset): Optional narrower event selector
        min_effect (float | Unset): Minimum |CAR| in standard deviations Default: 2.0.
        release_lag_days (int | None | Unset): Publication lag of the series; triggers a re-check and
            `LAGGED_PUBLICATION`
    """

    event_family: str
    region_codes: list[str] | Unset = UNSET
    selector: EventSelector | None | Unset = UNSET
    min_effect: float | Unset = 2.0
    release_lag_days: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.event_selector import EventSelector

        event_family = self.event_family

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        selector: dict[str, Any] | None | Unset
        if isinstance(self.selector, Unset):
            selector = UNSET
        elif isinstance(self.selector, EventSelector):
            selector = self.selector.to_dict()
        else:
            selector = self.selector

        min_effect = self.min_effect

        release_lag_days: int | None | Unset
        if isinstance(self.release_lag_days, Unset):
            release_lag_days = UNSET
        else:
            release_lag_days = self.release_lag_days

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "eventFamily": event_family,
            }
        )
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if selector is not UNSET:
            field_dict["selector"] = selector
        if min_effect is not UNSET:
            field_dict["minEffect"] = min_effect
        if release_lag_days is not UNSET:
            field_dict["releaseLagDays"] = release_lag_days

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.event_selector import EventSelector

        d = dict(src_dict)
        event_family = d.pop("eventFamily")

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        def _parse_selector(data: object) -> EventSelector | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                selector_type_0 = EventSelector.from_dict(data)

                return selector_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EventSelector | None | Unset, data)

        selector = _parse_selector(d.pop("selector", UNSET))

        min_effect = d.pop("minEffect", UNSET)

        def _parse_release_lag_days(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        release_lag_days = _parse_release_lag_days(d.pop("releaseLagDays", UNSET))

        watch_pair = cls(
            event_family=event_family,
            region_codes=region_codes,
            selector=selector,
            min_effect=min_effect,
            release_lag_days=release_lag_days,
        )

        watch_pair.additional_properties = d
        return watch_pair

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
