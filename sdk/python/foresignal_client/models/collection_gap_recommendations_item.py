from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.collection_gap_recommendations_item_type import (
    CollectionGapRecommendationsItemType,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="CollectionGapRecommendationsItem")


@_attrs_define
class CollectionGapRecommendationsItem:
    """Collection recommendation (never applied automatically)

    Attributes:
        type_ (CollectionGapRecommendationsItemType):
        text (str):
        source_id (None | str | Unset):
        sensor_family (None | str | Unset): e.g. 069, 078 or 086 receiver or sensor family
    """

    type_: CollectionGapRecommendationsItemType
    text: str
    source_id: None | str | Unset = UNSET
    sensor_family: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        text = self.text

        source_id: None | str | Unset
        if isinstance(self.source_id, Unset):
            source_id = UNSET
        else:
            source_id = self.source_id

        sensor_family: None | str | Unset
        if isinstance(self.sensor_family, Unset):
            sensor_family = UNSET
        else:
            sensor_family = self.sensor_family

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "text": text,
            }
        )
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if sensor_family is not UNSET:
            field_dict["sensorFamily"] = sensor_family

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = CollectionGapRecommendationsItemType(d.pop("type"))

        text = d.pop("text")

        def _parse_source_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source_id = _parse_source_id(d.pop("sourceId", UNSET))

        def _parse_sensor_family(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        sensor_family = _parse_sensor_family(d.pop("sensorFamily", UNSET))

        collection_gap_recommendations_item = cls(
            type_=type_,
            text=text,
            source_id=source_id,
            sensor_family=sensor_family,
        )

        collection_gap_recommendations_item.additional_properties = d
        return collection_gap_recommendations_item

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
