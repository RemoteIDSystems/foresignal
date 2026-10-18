from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tile_key_create_prefixes_item import TileKeyCreatePrefixesItem
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.read_restriction import ReadRestriction


T = TypeVar("T", bound="TileKeyCreate")


@_attrs_define
class TileKeyCreate:
    """Tile key creation request

    Attributes:
        name (str):
        prefixes (list[TileKeyCreatePrefixesItem]): Allowed canonical prefixes (anything else → 400 VALIDATION_FAILED)
        ttl (str): Lifetime, at most 30 days. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
        restrictions (ReadRestriction | Unset): The effective read restriction of a principal (035): restricted tokens,
            feed keys and the anonymous profile. Intersected with every request filter by `RestrictionClamp`; out-of-scope
            single reads return 404.
    """

    name: str
    prefixes: list[TileKeyCreatePrefixesItem]
    ttl: str
    restrictions: ReadRestriction | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        prefixes = []
        for prefixes_item_data in self.prefixes:
            prefixes_item = prefixes_item_data.value
            prefixes.append(prefixes_item)

        ttl = self.ttl

        restrictions: dict[str, Any] | Unset = UNSET
        if not isinstance(self.restrictions, Unset):
            restrictions = self.restrictions.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "prefixes": prefixes,
                "ttl": ttl,
            }
        )
        if restrictions is not UNSET:
            field_dict["restrictions"] = restrictions

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.read_restriction import ReadRestriction

        d = dict(src_dict)
        name = d.pop("name")

        prefixes = []
        _prefixes = d.pop("prefixes")
        for prefixes_item_data in _prefixes:
            prefixes_item = TileKeyCreatePrefixesItem(prefixes_item_data)

            prefixes.append(prefixes_item)

        ttl = d.pop("ttl")

        _restrictions = d.pop("restrictions", UNSET)
        restrictions: ReadRestriction | Unset
        if isinstance(_restrictions, Unset):
            restrictions = UNSET
        else:
            restrictions = ReadRestriction.from_dict(_restrictions)

        tile_key_create = cls(
            name=name,
            prefixes=prefixes,
            ttl=ttl,
            restrictions=restrictions,
        )

        tile_key_create.additional_properties = d
        return tile_key_create

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
