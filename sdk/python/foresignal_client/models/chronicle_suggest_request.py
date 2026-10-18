from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_area import ChronicleArea


T = TypeVar("T", bound="ChronicleSuggestRequest")


@_attrs_define
class ChronicleSuggestRequest:
    """Ask for a draft chronicle from a situation or an area and topic

    Attributes:
        situation_id (None | str | Unset):
        area (ChronicleArea | None | Unset):
        topic (None | str | Unset): 117 attention topic id or free keywords
    """

    situation_id: None | str | Unset = UNSET
    area: ChronicleArea | None | Unset = UNSET
    topic: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.chronicle_area import ChronicleArea

        situation_id: None | str | Unset
        if isinstance(self.situation_id, Unset):
            situation_id = UNSET
        else:
            situation_id = self.situation_id

        area: dict[str, Any] | None | Unset
        if isinstance(self.area, Unset):
            area = UNSET
        elif isinstance(self.area, ChronicleArea):
            area = self.area.to_dict()
        else:
            area = self.area

        topic: None | str | Unset
        if isinstance(self.topic, Unset):
            topic = UNSET
        else:
            topic = self.topic

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id
        if area is not UNSET:
            field_dict["area"] = area
        if topic is not UNSET:
            field_dict["topic"] = topic

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_area import ChronicleArea

        d = dict(src_dict)

        def _parse_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        situation_id = _parse_situation_id(d.pop("situationId", UNSET))

        def _parse_area(data: object) -> ChronicleArea | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                area_type_0 = ChronicleArea.from_dict(data)

                return area_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ChronicleArea | None | Unset, data)

        area = _parse_area(d.pop("area", UNSET))

        def _parse_topic(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        topic = _parse_topic(d.pop("topic", UNSET))

        chronicle_suggest_request = cls(
            situation_id=situation_id,
            area=area,
            topic=topic,
        )

        chronicle_suggest_request.additional_properties = d
        return chronicle_suggest_request

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
