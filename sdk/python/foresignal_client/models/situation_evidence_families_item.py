from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.situation_member import SituationMember


T = TypeVar("T", bound="SituationEvidenceFamiliesItem")


@_attrs_define
class SituationEvidenceFamiliesItem:
    """Members of one signal family (most recent first, up to 20)

    Attributes:
        family (str): Signal family from the 119 registry (`intel/signal-families.json`, operator-extensible), e.g.
            `ais-behaviour`, `aviation-military`, `aviation-ops`, `conflict`, `outage`, `internet-measurement`, `gnss-
            interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`, `markets`, `sanctions`,
            `health`, `space`
        count (int):
        members (list[SituationMember]):
        core_count (int | Unset):
        more (bool | Unset): True when the family has more members than listed
    """

    family: str
    count: int
    members: list[SituationMember]
    core_count: int | Unset = UNSET
    more: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        family = self.family

        count = self.count

        members = []
        for members_item_data in self.members:
            members_item = members_item_data.to_dict()
            members.append(members_item)

        core_count = self.core_count

        more = self.more

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "family": family,
                "count": count,
                "members": members,
            }
        )
        if core_count is not UNSET:
            field_dict["coreCount"] = core_count
        if more is not UNSET:
            field_dict["more"] = more

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_member import SituationMember

        d = dict(src_dict)
        family = d.pop("family")

        count = d.pop("count")

        members = []
        _members = d.pop("members")
        for members_item_data in _members:
            members_item = SituationMember.from_dict(members_item_data)

            members.append(members_item)

        core_count = d.pop("coreCount", UNSET)

        more = d.pop("more", UNSET)

        situation_evidence_families_item = cls(
            family=family,
            count=count,
            members=members,
            core_count=core_count,
            more=more,
        )

        situation_evidence_families_item.additional_properties = d
        return situation_evidence_families_item

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
