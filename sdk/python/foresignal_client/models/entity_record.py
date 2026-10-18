from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.entity_link import EntityLink
    from ..models.record import Record


T = TypeVar("T", bound="EntityRecord")


@_attrs_define
class EntityRecord:
    """A current record linked to an entity, with the link that joins them

    Attributes:
        record (Record):
        link (EntityLink): Time-bounded link from a source record to an entity, with the key that produced it
    """

    record: Record
    link: EntityLink
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        record = self.record.to_dict()

        link = self.link.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "record": record,
                "link": link,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.entity_link import EntityLink
        from ..models.record import Record

        d = dict(src_dict)
        record = Record.from_dict(d.pop("record"))

        link = EntityLink.from_dict(d.pop("link"))

        entity_record = cls(
            record=record,
            link=link,
        )

        entity_record.additional_properties = d
        return entity_record

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
