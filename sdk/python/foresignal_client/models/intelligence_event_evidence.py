from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.intel_event_member import IntelEventMember


T = TypeVar("T", bound="IntelligenceEventEvidence")


@_attrs_define
class IntelligenceEventEvidence:
    """Observed members

    Attributes:
        member_count (int):
        members (list[IntelEventMember]):
        signal_families (list[str] | Unset):
    """

    member_count: int
    members: list[IntelEventMember]
    signal_families: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        member_count = self.member_count

        members = []
        for members_item_data in self.members:
            members_item = members_item_data.to_dict()
            members.append(members_item)

        signal_families: list[str] | Unset = UNSET
        if not isinstance(self.signal_families, Unset):
            signal_families = self.signal_families

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "memberCount": member_count,
                "members": members,
            }
        )
        if signal_families is not UNSET:
            field_dict["signalFamilies"] = signal_families

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intel_event_member import IntelEventMember

        d = dict(src_dict)
        member_count = d.pop("memberCount")

        members = []
        _members = d.pop("members")
        for members_item_data in _members:
            members_item = IntelEventMember.from_dict(members_item_data)

            members.append(members_item)

        signal_families = cast(list[str], d.pop("signalFamilies", UNSET))

        intelligence_event_evidence = cls(
            member_count=member_count,
            members=members,
            signal_families=signal_families,
        )

        intelligence_event_evidence.additional_properties = d
        return intelligence_event_evidence

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
