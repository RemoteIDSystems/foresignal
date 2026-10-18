from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.collection_gap_recommendations_item import (
        CollectionGapRecommendationsItem,
    )


T = TypeVar("T", bound="CollectionGap")


@_attrs_define
class CollectionGap:
    """An indicator that would matter but cannot currently be observed

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        indicator_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        potential_eig (float):
        actual_eig (float):
        recommendations (list[CollectionGapRecommendationsItem]):
        p_detect (float | None | Unset):
        created_at (datetime.datetime | Unset):
        updated_at (datetime.datetime | Unset):
    """

    id: str
    set_id: str
    indicator_id: str
    potential_eig: float
    actual_eig: float
    recommendations: list[CollectionGapRecommendationsItem]
    p_detect: float | None | Unset = UNSET
    created_at: datetime.datetime | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        set_id = self.set_id

        indicator_id = self.indicator_id

        potential_eig = self.potential_eig

        actual_eig = self.actual_eig

        recommendations = []
        for recommendations_item_data in self.recommendations:
            recommendations_item = recommendations_item_data.to_dict()
            recommendations.append(recommendations_item)

        p_detect: float | None | Unset
        if isinstance(self.p_detect, Unset):
            p_detect = UNSET
        else:
            p_detect = self.p_detect

        created_at: str | Unset = UNSET
        if not isinstance(self.created_at, Unset):
            created_at = self.created_at.isoformat()

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "setId": set_id,
                "indicatorId": indicator_id,
                "potentialEig": potential_eig,
                "actualEig": actual_eig,
                "recommendations": recommendations,
            }
        )
        if p_detect is not UNSET:
            field_dict["pDetect"] = p_detect
        if created_at is not UNSET:
            field_dict["createdAt"] = created_at
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.collection_gap_recommendations_item import (
            CollectionGapRecommendationsItem,
        )

        d = dict(src_dict)
        id = d.pop("id")

        set_id = d.pop("setId")

        indicator_id = d.pop("indicatorId")

        potential_eig = d.pop("potentialEig")

        actual_eig = d.pop("actualEig")

        recommendations = []
        _recommendations = d.pop("recommendations")
        for recommendations_item_data in _recommendations:
            recommendations_item = CollectionGapRecommendationsItem.from_dict(
                recommendations_item_data
            )

            recommendations.append(recommendations_item)

        def _parse_p_detect(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_detect = _parse_p_detect(d.pop("pDetect", UNSET))

        _created_at = d.pop("createdAt", UNSET)
        created_at: datetime.datetime | Unset
        if isinstance(_created_at, Unset):
            created_at = UNSET
        else:
            created_at = datetime.datetime.fromisoformat(_created_at)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        collection_gap = cls(
            id=id,
            set_id=set_id,
            indicator_id=indicator_id,
            potential_eig=potential_eig,
            actual_eig=actual_eig,
            recommendations=recommendations,
            p_detect=p_detect,
            created_at=created_at,
            updated_at=updated_at,
        )

        collection_gap.additional_properties = d
        return collection_gap

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
