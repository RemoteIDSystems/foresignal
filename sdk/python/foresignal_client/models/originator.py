from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.originator_class import OriginatorClass
from ..types import UNSET, Unset

T = TypeVar("T", bound="Originator")


@_attrs_define
class Originator:
    """Who originated an observation (116)

    Attributes:
        id (str):  Example: usgs-anss.
        class_ (OriginatorClass):
        independence_group (None | str | Unset): Sources in the same group are never independent of each other
        aggregates (list[str] | Unset): Originator ids this originator republishes
        notes (None | str | Unset):
    """

    id: str
    class_: OriginatorClass
    independence_group: None | str | Unset = UNSET
    aggregates: list[str] | Unset = UNSET
    notes: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        class_ = self.class_.value

        independence_group: None | str | Unset
        if isinstance(self.independence_group, Unset):
            independence_group = UNSET
        else:
            independence_group = self.independence_group

        aggregates: list[str] | Unset = UNSET
        if not isinstance(self.aggregates, Unset):
            aggregates = self.aggregates

        notes: None | str | Unset
        if isinstance(self.notes, Unset):
            notes = UNSET
        else:
            notes = self.notes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "class": class_,
            }
        )
        if independence_group is not UNSET:
            field_dict["independenceGroup"] = independence_group
        if aggregates is not UNSET:
            field_dict["aggregates"] = aggregates
        if notes is not UNSET:
            field_dict["notes"] = notes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        class_ = OriginatorClass(d.pop("class"))

        def _parse_independence_group(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        independence_group = _parse_independence_group(
            d.pop("independenceGroup", UNSET)
        )

        aggregates = cast(list[str], d.pop("aggregates", UNSET))

        def _parse_notes(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        notes = _parse_notes(d.pop("notes", UNSET))

        originator = cls(
            id=id,
            class_=class_,
            independence_group=independence_group,
            aggregates=aggregates,
            notes=notes,
        )

        originator.additional_properties = d
        return originator

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
