from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_cluster_members_item_disclosure import (
    ChronicleClusterMembersItemDisclosure,
)
from ..models.chronicle_cluster_members_item_strength import (
    ChronicleClusterMembersItemStrength,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.object_envelope import ObjectEnvelope


T = TypeVar("T", bound="ChronicleClusterMembersItem")


@_attrs_define
class ChronicleClusterMembersItem:
    """A cluster member

    Attributes:
        disclosure (ChronicleClusterMembersItemDisclosure):
        chronicle_id (None | str | Unset): Only for chronicles the caller can see
        envelope (None | ObjectEnvelope | Unset):
        link_id (None | str | Unset):
        strength (ChronicleClusterMembersItemStrength | Unset): From estimated Jaccard: low < 0.1 ≤ medium < 0.3 ≤ high
    """

    disclosure: ChronicleClusterMembersItemDisclosure
    chronicle_id: None | str | Unset = UNSET
    envelope: None | ObjectEnvelope | Unset = UNSET
    link_id: None | str | Unset = UNSET
    strength: ChronicleClusterMembersItemStrength | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.object_envelope import ObjectEnvelope

        disclosure = self.disclosure.value

        chronicle_id: None | str | Unset
        if isinstance(self.chronicle_id, Unset):
            chronicle_id = UNSET
        else:
            chronicle_id = self.chronicle_id

        envelope: dict[str, Any] | None | Unset
        if isinstance(self.envelope, Unset):
            envelope = UNSET
        elif isinstance(self.envelope, ObjectEnvelope):
            envelope = self.envelope.to_dict()
        else:
            envelope = self.envelope

        link_id: None | str | Unset
        if isinstance(self.link_id, Unset):
            link_id = UNSET
        else:
            link_id = self.link_id

        strength: str | Unset = UNSET
        if not isinstance(self.strength, Unset):
            strength = self.strength.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "disclosure": disclosure,
            }
        )
        if chronicle_id is not UNSET:
            field_dict["chronicleId"] = chronicle_id
        if envelope is not UNSET:
            field_dict["envelope"] = envelope
        if link_id is not UNSET:
            field_dict["linkId"] = link_id
        if strength is not UNSET:
            field_dict["strength"] = strength

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.object_envelope import ObjectEnvelope

        d = dict(src_dict)
        disclosure = ChronicleClusterMembersItemDisclosure(d.pop("disclosure"))

        def _parse_chronicle_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        chronicle_id = _parse_chronicle_id(d.pop("chronicleId", UNSET))

        def _parse_envelope(data: object) -> None | ObjectEnvelope | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                envelope_type_0 = ObjectEnvelope.from_dict(data)

                return envelope_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ObjectEnvelope | Unset, data)

        envelope = _parse_envelope(d.pop("envelope", UNSET))

        def _parse_link_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        link_id = _parse_link_id(d.pop("linkId", UNSET))

        _strength = d.pop("strength", UNSET)
        strength: ChronicleClusterMembersItemStrength | Unset
        if isinstance(_strength, Unset):
            strength = UNSET
        else:
            strength = ChronicleClusterMembersItemStrength(_strength)

        chronicle_cluster_members_item = cls(
            disclosure=disclosure,
            chronicle_id=chronicle_id,
            envelope=envelope,
            link_id=link_id,
            strength=strength,
        )

        chronicle_cluster_members_item.additional_properties = d
        return chronicle_cluster_members_item

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
