from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelEventMemberJoinReason")


@_attrs_define
class IntelEventMemberJoinReason:
    """Why the member joined

    Attributes:
        clause (None | str | Unset): Recipe clause (seed, join index, contradiction)
        distance_km (float | None | Unset):
        dt_minutes (float | None | Unset):
        matched_keys (list[str] | Unset):
        region_tag (None | str | Unset):
    """

    clause: None | str | Unset = UNSET
    distance_km: float | None | Unset = UNSET
    dt_minutes: float | None | Unset = UNSET
    matched_keys: list[str] | Unset = UNSET
    region_tag: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        clause: None | str | Unset
        if isinstance(self.clause, Unset):
            clause = UNSET
        else:
            clause = self.clause

        distance_km: float | None | Unset
        if isinstance(self.distance_km, Unset):
            distance_km = UNSET
        else:
            distance_km = self.distance_km

        dt_minutes: float | None | Unset
        if isinstance(self.dt_minutes, Unset):
            dt_minutes = UNSET
        else:
            dt_minutes = self.dt_minutes

        matched_keys: list[str] | Unset = UNSET
        if not isinstance(self.matched_keys, Unset):
            matched_keys = self.matched_keys

        region_tag: None | str | Unset
        if isinstance(self.region_tag, Unset):
            region_tag = UNSET
        else:
            region_tag = self.region_tag

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if clause is not UNSET:
            field_dict["clause"] = clause
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km
        if dt_minutes is not UNSET:
            field_dict["dtMinutes"] = dt_minutes
        if matched_keys is not UNSET:
            field_dict["matchedKeys"] = matched_keys
        if region_tag is not UNSET:
            field_dict["regionTag"] = region_tag

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_clause(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        clause = _parse_clause(d.pop("clause", UNSET))

        def _parse_distance_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        distance_km = _parse_distance_km(d.pop("distanceKm", UNSET))

        def _parse_dt_minutes(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        dt_minutes = _parse_dt_minutes(d.pop("dtMinutes", UNSET))

        matched_keys = cast(list[str], d.pop("matchedKeys", UNSET))

        def _parse_region_tag(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region_tag = _parse_region_tag(d.pop("regionTag", UNSET))

        intel_event_member_join_reason = cls(
            clause=clause,
            distance_km=distance_km,
            dt_minutes=dt_minutes,
            matched_keys=matched_keys,
            region_tag=region_tag,
        )

        intel_event_member_join_reason.additional_properties = d
        return intel_event_member_join_reason

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
