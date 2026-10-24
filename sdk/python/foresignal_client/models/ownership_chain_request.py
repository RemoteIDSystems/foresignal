from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ownership_chain_request_direction import OwnershipChainRequestDirection
from ..models.ownership_chain_request_types_item import OwnershipChainRequestTypesItem
from ..types import UNSET, Unset

T = TypeVar("T", bound="OwnershipChainRequest")


@_attrs_define
class OwnershipChainRequest:
    """Seed entities (people / organizations) whose ownership and affiliation chains to walk (158)

    Attributes:
        refs (list[str]):
        depth (int | Unset): Most ownership / affiliation hops to follow Default: 6.
        direction (OwnershipChainRequestDirection | Unset): Follow edges out of the seed (what it owns / controls), into
            it (who owns / controls it), or both Default: OwnershipChainRequestDirection.BOTH.
        types (list[OwnershipChainRequestTypesItem] | Unset): Ownership / affiliation edge types to walk; empty or
            absent walks all five
        min_confidence (float | Unset):  Default: 0.0.
        as_of (datetime.datetime | None | Unset):
    """

    refs: list[str]
    depth: int | Unset = 6
    direction: OwnershipChainRequestDirection | Unset = (
        OwnershipChainRequestDirection.BOTH
    )
    types: list[OwnershipChainRequestTypesItem] | Unset = UNSET
    min_confidence: float | Unset = 0.0
    as_of: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        refs = self.refs

        depth = self.depth

        direction: str | Unset = UNSET
        if not isinstance(self.direction, Unset):
            direction = self.direction.value

        types: list[str] | Unset = UNSET
        if not isinstance(self.types, Unset):
            types = []
            for types_item_data in self.types:
                types_item = types_item_data.value
                types.append(types_item)

        min_confidence = self.min_confidence

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "refs": refs,
            }
        )
        if depth is not UNSET:
            field_dict["depth"] = depth
        if direction is not UNSET:
            field_dict["direction"] = direction
        if types is not UNSET:
            field_dict["types"] = types
        if min_confidence is not UNSET:
            field_dict["minConfidence"] = min_confidence
        if as_of is not UNSET:
            field_dict["asOf"] = as_of

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        refs = cast(list[str], d.pop("refs"))

        depth = d.pop("depth", UNSET)

        _direction = d.pop("direction", UNSET)
        direction: OwnershipChainRequestDirection | Unset
        if isinstance(_direction, Unset):
            direction = UNSET
        else:
            direction = OwnershipChainRequestDirection(_direction)

        _types = d.pop("types", UNSET)
        types: list[OwnershipChainRequestTypesItem] | Unset = UNSET
        if _types is not UNSET:
            types = []
            for types_item_data in _types:
                types_item = OwnershipChainRequestTypesItem(types_item_data)

                types.append(types_item)

        min_confidence = d.pop("minConfidence", UNSET)

        def _parse_as_of(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                as_of_type_0 = datetime.datetime.fromisoformat(data)

                return as_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        as_of = _parse_as_of(d.pop("asOf", UNSET))

        ownership_chain_request = cls(
            refs=refs,
            depth=depth,
            direction=direction,
            types=types,
            min_confidence=min_confidence,
            as_of=as_of,
        )

        ownership_chain_request.additional_properties = d
        return ownership_chain_request

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
