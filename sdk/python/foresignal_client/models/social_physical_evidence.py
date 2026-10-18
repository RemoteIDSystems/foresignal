from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialPhysicalEvidence")


@_attrs_define
class SocialPhysicalEvidence:
    """Non-social evidence matching a claim's family, place and window

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        family (str):
        originator (str):
        first_observed_at (datetime.datetime):
        distance_km (float | None | Unset):
        negates (bool | Unset): An agency or sensor record that explicitly negates the claim (e.g. normal service)
    """

    ref: str
    family: str
    originator: str
    first_observed_at: datetime.datetime
    distance_km: float | None | Unset = UNSET
    negates: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        family = self.family

        originator = self.originator

        first_observed_at = self.first_observed_at.isoformat()

        distance_km: float | None | Unset
        if isinstance(self.distance_km, Unset):
            distance_km = UNSET
        else:
            distance_km = self.distance_km

        negates = self.negates

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "family": family,
                "originator": originator,
                "firstObservedAt": first_observed_at,
            }
        )
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km
        if negates is not UNSET:
            field_dict["negates"] = negates

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ref = d.pop("ref")

        family = d.pop("family")

        originator = d.pop("originator")

        first_observed_at = datetime.datetime.fromisoformat(d.pop("firstObservedAt"))

        def _parse_distance_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        distance_km = _parse_distance_km(d.pop("distanceKm", UNSET))

        negates = d.pop("negates", UNSET)

        social_physical_evidence = cls(
            ref=ref,
            family=family,
            originator=originator,
            first_observed_at=first_observed_at,
            distance_km=distance_km,
            negates=negates,
        )

        social_physical_evidence.additional_properties = d
        return social_physical_evidence

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
