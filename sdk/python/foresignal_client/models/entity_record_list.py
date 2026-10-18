from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.entity_record import EntityRecord


T = TypeVar("T", bound="EntityRecordList")


@_attrs_define
class EntityRecordList:
    """Current records across sources for one entity (head ⋈ live)

    Attributes:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        items (list[EntityRecord]):
        at (datetime.datetime | None | Unset): Validity instant the links were selected at
    """

    entity_id: str
    items: list[EntityRecord]
    at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        entity_id = self.entity_id

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        at: None | str | Unset
        if isinstance(self.at, Unset):
            at = UNSET
        elif isinstance(self.at, datetime.datetime):
            at = self.at.isoformat()
        else:
            at = self.at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "entityId": entity_id,
                "items": items,
            }
        )
        if at is not UNSET:
            field_dict["at"] = at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.entity_record import EntityRecord

        d = dict(src_dict)
        entity_id = d.pop("entityId")

        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = EntityRecord.from_dict(items_item_data)

            items.append(items_item)

        def _parse_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                at_type_0 = datetime.datetime.fromisoformat(data)

                return at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        at = _parse_at(d.pop("at", UNSET))

        entity_record_list = cls(
            entity_id=entity_id,
            items=items,
            at=at,
        )

        entity_record_list.additional_properties = d
        return entity_record_list

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
