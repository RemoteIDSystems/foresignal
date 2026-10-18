from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.observation_link_link_reason_attribute_deltas import (
        ObservationLinkLinkReasonAttributeDeltas,
    )


T = TypeVar("T", bound="ObservationLinkLinkReason")


@_attrs_define
class ObservationLinkLinkReason:
    """Why the observation was linked

    Attributes:
        rule (str):
        dt_seconds (float | None | Unset):
        distance_km (float | None | Unset):
        attribute_deltas (ObservationLinkLinkReasonAttributeDeltas | Unset):
        upstream_id_match (bool | Unset):
    """

    rule: str
    dt_seconds: float | None | Unset = UNSET
    distance_km: float | None | Unset = UNSET
    attribute_deltas: ObservationLinkLinkReasonAttributeDeltas | Unset = UNSET
    upstream_id_match: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rule = self.rule

        dt_seconds: float | None | Unset
        if isinstance(self.dt_seconds, Unset):
            dt_seconds = UNSET
        else:
            dt_seconds = self.dt_seconds

        distance_km: float | None | Unset
        if isinstance(self.distance_km, Unset):
            distance_km = UNSET
        else:
            distance_km = self.distance_km

        attribute_deltas: dict[str, Any] | Unset = UNSET
        if not isinstance(self.attribute_deltas, Unset):
            attribute_deltas = self.attribute_deltas.to_dict()

        upstream_id_match = self.upstream_id_match

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "rule": rule,
            }
        )
        if dt_seconds is not UNSET:
            field_dict["dtSeconds"] = dt_seconds
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km
        if attribute_deltas is not UNSET:
            field_dict["attributeDeltas"] = attribute_deltas
        if upstream_id_match is not UNSET:
            field_dict["upstreamIdMatch"] = upstream_id_match

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.observation_link_link_reason_attribute_deltas import (
            ObservationLinkLinkReasonAttributeDeltas,
        )

        d = dict(src_dict)
        rule = d.pop("rule")

        def _parse_dt_seconds(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        dt_seconds = _parse_dt_seconds(d.pop("dtSeconds", UNSET))

        def _parse_distance_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        distance_km = _parse_distance_km(d.pop("distanceKm", UNSET))

        _attribute_deltas = d.pop("attributeDeltas", UNSET)
        attribute_deltas: ObservationLinkLinkReasonAttributeDeltas | Unset
        if isinstance(_attribute_deltas, Unset):
            attribute_deltas = UNSET
        else:
            attribute_deltas = ObservationLinkLinkReasonAttributeDeltas.from_dict(
                _attribute_deltas
            )

        upstream_id_match = d.pop("upstreamIdMatch", UNSET)

        observation_link_link_reason = cls(
            rule=rule,
            dt_seconds=dt_seconds,
            distance_km=distance_km,
            attribute_deltas=attribute_deltas,
            upstream_id_match=upstream_id_match,
        )

        observation_link_link_reason.additional_properties = d
        return observation_link_link_reason

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
