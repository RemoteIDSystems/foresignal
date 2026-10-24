from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelCorroborationAutoCorroborate")


@_attrs_define
class IntelCorroborationAutoCorroborate:
    """Automatic corroboration of a lone agent/analyst/operator event on creation: it searches for independent observations
    near it in space and time and attaches them, so it surfaces already corroborated and can auto-activate

        Attributes:
            enabled (bool | Unset): Whether a lone event searches for corroboration on creation (default true)
            within_km (float | Unset): The search radius around the event's members, in kilometers (default 50)
            window (str | Unset): The time window before and after the members' observation span, an ISO-8601 or shorthand
                duration (default PT24H)
            max_members (int | Unset): The most corroborating members one search attaches, one per independent originator
                root (default 10)
    """

    enabled: bool | Unset = UNSET
    within_km: float | Unset = UNSET
    window: str | Unset = UNSET
    max_members: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        within_km = self.within_km

        window = self.window

        max_members = self.max_members

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if within_km is not UNSET:
            field_dict["withinKm"] = within_km
        if window is not UNSET:
            field_dict["window"] = window
        if max_members is not UNSET:
            field_dict["maxMembers"] = max_members

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        enabled = d.pop("enabled", UNSET)

        within_km = d.pop("withinKm", UNSET)

        window = d.pop("window", UNSET)

        max_members = d.pop("maxMembers", UNSET)

        intel_corroboration_auto_corroborate = cls(
            enabled=enabled,
            within_km=within_km,
            window=window,
            max_members=max_members,
        )

        intel_corroboration_auto_corroborate.additional_properties = d
        return intel_corroboration_auto_corroborate

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
