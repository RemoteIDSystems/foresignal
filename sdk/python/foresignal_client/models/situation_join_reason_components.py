from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SituationJoinReasonComponents")


@_attrs_define
class SituationJoinReasonComponents:
    """Score components (documented constants in `LinkerWeights`)

    Attributes:
        region (float | None | Unset): Region contribution (0.30 shared admin1, 0.20 shared country)
        distance_km (float | None | Unset): Distance to the situation geometry in km (contributes 0.25 × max(0, 1 − d /
            maxDistanceKm))
        entity (float | None | Unset): Shared 098 entity contribution (0.25)
        topic (float | None | Unset): 117 attention-topic keyword match contribution (0.15)
        text_similarity (float | None | Unset): Embedding similarity contribution, 0.20 × max(0, (cos − 0.70) / 0.30)
        theme (float | None | Unset): Theme incompatibility penalty (−0.20 or 0)
        recency_hours (float | None | Unset): Hours since the situation's last activity (recency multiplier 0.5 + 0.5 ×
            exp(−h / 72))
    """

    region: float | None | Unset = UNSET
    distance_km: float | None | Unset = UNSET
    entity: float | None | Unset = UNSET
    topic: float | None | Unset = UNSET
    text_similarity: float | None | Unset = UNSET
    theme: float | None | Unset = UNSET
    recency_hours: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        region: float | None | Unset
        if isinstance(self.region, Unset):
            region = UNSET
        else:
            region = self.region

        distance_km: float | None | Unset
        if isinstance(self.distance_km, Unset):
            distance_km = UNSET
        else:
            distance_km = self.distance_km

        entity: float | None | Unset
        if isinstance(self.entity, Unset):
            entity = UNSET
        else:
            entity = self.entity

        topic: float | None | Unset
        if isinstance(self.topic, Unset):
            topic = UNSET
        else:
            topic = self.topic

        text_similarity: float | None | Unset
        if isinstance(self.text_similarity, Unset):
            text_similarity = UNSET
        else:
            text_similarity = self.text_similarity

        theme: float | None | Unset
        if isinstance(self.theme, Unset):
            theme = UNSET
        else:
            theme = self.theme

        recency_hours: float | None | Unset
        if isinstance(self.recency_hours, Unset):
            recency_hours = UNSET
        else:
            recency_hours = self.recency_hours

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if region is not UNSET:
            field_dict["region"] = region
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km
        if entity is not UNSET:
            field_dict["entity"] = entity
        if topic is not UNSET:
            field_dict["topic"] = topic
        if text_similarity is not UNSET:
            field_dict["textSimilarity"] = text_similarity
        if theme is not UNSET:
            field_dict["theme"] = theme
        if recency_hours is not UNSET:
            field_dict["recencyHours"] = recency_hours

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_region(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        region = _parse_region(d.pop("region", UNSET))

        def _parse_distance_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        distance_km = _parse_distance_km(d.pop("distanceKm", UNSET))

        def _parse_entity(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        entity = _parse_entity(d.pop("entity", UNSET))

        def _parse_topic(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        topic = _parse_topic(d.pop("topic", UNSET))

        def _parse_text_similarity(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        text_similarity = _parse_text_similarity(d.pop("textSimilarity", UNSET))

        def _parse_theme(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        theme = _parse_theme(d.pop("theme", UNSET))

        def _parse_recency_hours(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        recency_hours = _parse_recency_hours(d.pop("recencyHours", UNSET))

        situation_join_reason_components = cls(
            region=region,
            distance_km=distance_km,
            entity=entity,
            topic=topic,
            text_similarity=text_similarity,
            theme=theme,
            recency_hours=recency_hours,
        )

        situation_join_reason_components.additional_properties = d
        return situation_join_reason_components

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
