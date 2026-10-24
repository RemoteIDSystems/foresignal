from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ownership_chain_direction import OwnershipChainDirection
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ownership_chain_node import OwnershipChainNode


T = TypeVar("T", bound="OwnershipChain")


@_attrs_define
class OwnershipChain:
    """Ownership / affiliation chains from the seeds and the attributes they share (158)

    Attributes:
        refs (list[str]):
        direction (OwnershipChainDirection):
        chains (list[OwnershipChainNode]): Every entity reached from a seed through ownership / affiliation edges,
            strongest and shortest path first
        shared (list[OwnershipChainNode]): The subset of `chains` reached from two or more seeds — a shared owner,
            officer, address or affiliate
        as_of (datetime.datetime | None | Unset):
        generated_at (datetime.datetime | Unset):
    """

    refs: list[str]
    direction: OwnershipChainDirection
    chains: list[OwnershipChainNode]
    shared: list[OwnershipChainNode]
    as_of: datetime.datetime | None | Unset = UNSET
    generated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        refs = self.refs

        direction = self.direction.value

        chains = []
        for chains_item_data in self.chains:
            chains_item = chains_item_data.to_dict()
            chains.append(chains_item)

        shared = []
        for shared_item_data in self.shared:
            shared_item = shared_item_data.to_dict()
            shared.append(shared_item)

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        generated_at: str | Unset = UNSET
        if not isinstance(self.generated_at, Unset):
            generated_at = self.generated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "refs": refs,
                "direction": direction,
                "chains": chains,
                "shared": shared,
            }
        )
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if generated_at is not UNSET:
            field_dict["generatedAt"] = generated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ownership_chain_node import OwnershipChainNode

        d = dict(src_dict)
        refs = cast(list[str], d.pop("refs"))

        direction = OwnershipChainDirection(d.pop("direction"))

        chains = []
        _chains = d.pop("chains")
        for chains_item_data in _chains:
            chains_item = OwnershipChainNode.from_dict(chains_item_data)

            chains.append(chains_item)

        shared = []
        _shared = d.pop("shared")
        for shared_item_data in _shared:
            shared_item = OwnershipChainNode.from_dict(shared_item_data)

            shared.append(shared_item)

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

        _generated_at = d.pop("generatedAt", UNSET)
        generated_at: datetime.datetime | Unset
        if isinstance(_generated_at, Unset):
            generated_at = UNSET
        else:
            generated_at = datetime.datetime.fromisoformat(_generated_at)

        ownership_chain = cls(
            refs=refs,
            direction=direction,
            chains=chains,
            shared=shared,
            as_of=as_of,
            generated_at=generated_at,
        )

        ownership_chain.additional_properties = d
        return ownership_chain

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
