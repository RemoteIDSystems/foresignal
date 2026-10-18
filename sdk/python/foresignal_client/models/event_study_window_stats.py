from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.event_study_window_stats_placebo import EventStudyWindowStatsPlacebo


T = TypeVar("T", bound="EventStudyWindowStats")


@_attrs_define
class EventStudyWindowStats:
    """Cross-event statistics for one window

    Attributes:
        window (list[int]): Offsets in buckets, inclusive
        caar (float | None | Unset): Cumulative average abnormal value (standardised when values are withheld)
        standardised_z (float | None | Unset):
        standardised_p (float | None | Unset):
        corrado_stat (float | None | Unset):
        corrado_p (float | None | Unset):
        placebo_p (float | None | Unset): Share of placebo windows at least as extreme
        placebo (EventStudyWindowStatsPlacebo | Unset): Placebo distribution of the same statistic
    """

    window: list[int]
    caar: float | None | Unset = UNSET
    standardised_z: float | None | Unset = UNSET
    standardised_p: float | None | Unset = UNSET
    corrado_stat: float | None | Unset = UNSET
    corrado_p: float | None | Unset = UNSET
    placebo_p: float | None | Unset = UNSET
    placebo: EventStudyWindowStatsPlacebo | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        window = self.window

        caar: float | None | Unset
        if isinstance(self.caar, Unset):
            caar = UNSET
        else:
            caar = self.caar

        standardised_z: float | None | Unset
        if isinstance(self.standardised_z, Unset):
            standardised_z = UNSET
        else:
            standardised_z = self.standardised_z

        standardised_p: float | None | Unset
        if isinstance(self.standardised_p, Unset):
            standardised_p = UNSET
        else:
            standardised_p = self.standardised_p

        corrado_stat: float | None | Unset
        if isinstance(self.corrado_stat, Unset):
            corrado_stat = UNSET
        else:
            corrado_stat = self.corrado_stat

        corrado_p: float | None | Unset
        if isinstance(self.corrado_p, Unset):
            corrado_p = UNSET
        else:
            corrado_p = self.corrado_p

        placebo_p: float | None | Unset
        if isinstance(self.placebo_p, Unset):
            placebo_p = UNSET
        else:
            placebo_p = self.placebo_p

        placebo: dict[str, Any] | Unset = UNSET
        if not isinstance(self.placebo, Unset):
            placebo = self.placebo.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "window": window,
            }
        )
        if caar is not UNSET:
            field_dict["caar"] = caar
        if standardised_z is not UNSET:
            field_dict["standardisedZ"] = standardised_z
        if standardised_p is not UNSET:
            field_dict["standardisedP"] = standardised_p
        if corrado_stat is not UNSET:
            field_dict["corradoStat"] = corrado_stat
        if corrado_p is not UNSET:
            field_dict["corradoP"] = corrado_p
        if placebo_p is not UNSET:
            field_dict["placeboP"] = placebo_p
        if placebo is not UNSET:
            field_dict["placebo"] = placebo

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.event_study_window_stats_placebo import (
            EventStudyWindowStatsPlacebo,
        )

        d = dict(src_dict)
        window = cast(list[int], d.pop("window"))

        def _parse_caar(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        caar = _parse_caar(d.pop("caar", UNSET))

        def _parse_standardised_z(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        standardised_z = _parse_standardised_z(d.pop("standardisedZ", UNSET))

        def _parse_standardised_p(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        standardised_p = _parse_standardised_p(d.pop("standardisedP", UNSET))

        def _parse_corrado_stat(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        corrado_stat = _parse_corrado_stat(d.pop("corradoStat", UNSET))

        def _parse_corrado_p(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        corrado_p = _parse_corrado_p(d.pop("corradoP", UNSET))

        def _parse_placebo_p(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        placebo_p = _parse_placebo_p(d.pop("placeboP", UNSET))

        _placebo = d.pop("placebo", UNSET)
        placebo: EventStudyWindowStatsPlacebo | Unset
        if isinstance(_placebo, Unset):
            placebo = UNSET
        else:
            placebo = EventStudyWindowStatsPlacebo.from_dict(_placebo)

        event_study_window_stats = cls(
            window=window,
            caar=caar,
            standardised_z=standardised_z,
            standardised_p=standardised_p,
            corrado_stat=corrado_stat,
            corrado_p=corrado_p,
            placebo_p=placebo_p,
            placebo=placebo,
        )

        event_study_window_stats.additional_properties = d
        return event_study_window_stats

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
