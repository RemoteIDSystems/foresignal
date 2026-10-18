from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="DigestActive")


@_attrs_define
class DigestActive:
    """Entities observed in the period (from record_live, including touch-only activity) (029)

    Attributes:
        source_id (str):
        kind (str):
        entities (int):
        previous (int | None | Unset): The previous period's count; null (record_live keeps only the latest receipt, so
            it is not known until 038's rollups)
    """

    source_id: str
    kind: str
    entities: int
    previous: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        kind = self.kind

        entities = self.entities

        previous: int | None | Unset
        if isinstance(self.previous, Unset):
            previous = UNSET
        else:
            previous = self.previous

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "kind": kind,
                "entities": entities,
            }
        )
        if previous is not UNSET:
            field_dict["previous"] = previous

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        kind = d.pop("kind")

        entities = d.pop("entities")

        def _parse_previous(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        previous = _parse_previous(d.pop("previous", UNSET))

        digest_active = cls(
            source_id=source_id,
            kind=kind,
            entities=entities,
            previous=previous,
        )

        digest_active.additional_properties = d
        return digest_active

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
