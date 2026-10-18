from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="AlertContextAnchor")


@_attrs_define
class AlertContextAnchor:
    """Moving-anchor ("near me") rules (036): the anchor record, its seq and the km-rounded distance to the target (never
    coordinates)

        Attributes:
            source (str | Unset):
            external_id (str | Unset):
            seq (int | None | Unset):
            distance_km (float | None | Unset):
    """

    source: str | Unset = UNSET
    external_id: str | Unset = UNSET
    seq: int | None | Unset = UNSET
    distance_km: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source = self.source

        external_id = self.external_id

        seq: int | None | Unset
        if isinstance(self.seq, Unset):
            seq = UNSET
        else:
            seq = self.seq

        distance_km: float | None | Unset
        if isinstance(self.distance_km, Unset):
            distance_km = UNSET
        else:
            distance_km = self.distance_km

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if source is not UNSET:
            field_dict["source"] = source
        if external_id is not UNSET:
            field_dict["externalId"] = external_id
        if seq is not UNSET:
            field_dict["seq"] = seq
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source = d.pop("source", UNSET)

        external_id = d.pop("externalId", UNSET)

        def _parse_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        seq = _parse_seq(d.pop("seq", UNSET))

        def _parse_distance_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        distance_km = _parse_distance_km(d.pop("distanceKm", UNSET))

        alert_context_anchor = cls(
            source=source,
            external_id=external_id,
            seq=seq,
            distance_km=distance_km,
        )

        alert_context_anchor.additional_properties = d
        return alert_context_anchor

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
