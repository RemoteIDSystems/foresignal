from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.propagation_node_type import PropagationNodeType
from ..types import UNSET, Unset

T = TypeVar("T", bound="PropagationNode")


@_attrs_define
class PropagationNode:
    """A propagation graph node

    Attributes:
        id (str):
        type_ (PropagationNodeType):
        observed_at (datetime.datetime):
        ref (None | str | Unset):
        platform (None | str | Unset):
        evidence_group_id (None | str | Unset):
        member_count (int | Unset): Posts collapsed into an `evidence-group` node
        label (None | str | Unset):
    """

    id: str
    type_: PropagationNodeType
    observed_at: datetime.datetime
    ref: None | str | Unset = UNSET
    platform: None | str | Unset = UNSET
    evidence_group_id: None | str | Unset = UNSET
    member_count: int | Unset = UNSET
    label: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        type_ = self.type_.value

        observed_at = self.observed_at.isoformat()

        ref: None | str | Unset
        if isinstance(self.ref, Unset):
            ref = UNSET
        else:
            ref = self.ref

        platform: None | str | Unset
        if isinstance(self.platform, Unset):
            platform = UNSET
        else:
            platform = self.platform

        evidence_group_id: None | str | Unset
        if isinstance(self.evidence_group_id, Unset):
            evidence_group_id = UNSET
        else:
            evidence_group_id = self.evidence_group_id

        member_count = self.member_count

        label: None | str | Unset
        if isinstance(self.label, Unset):
            label = UNSET
        else:
            label = self.label

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
                "observedAt": observed_at,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if platform is not UNSET:
            field_dict["platform"] = platform
        if evidence_group_id is not UNSET:
            field_dict["evidenceGroupId"] = evidence_group_id
        if member_count is not UNSET:
            field_dict["memberCount"] = member_count
        if label is not UNSET:
            field_dict["label"] = label

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        type_ = PropagationNodeType(d.pop("type"))

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        def _parse_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ref = _parse_ref(d.pop("ref", UNSET))

        def _parse_platform(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        platform = _parse_platform(d.pop("platform", UNSET))

        def _parse_evidence_group_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        evidence_group_id = _parse_evidence_group_id(d.pop("evidenceGroupId", UNSET))

        member_count = d.pop("memberCount", UNSET)

        def _parse_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        label = _parse_label(d.pop("label", UNSET))

        propagation_node = cls(
            id=id,
            type_=type_,
            observed_at=observed_at,
            ref=ref,
            platform=platform,
            evidence_group_id=evidence_group_id,
            member_count=member_count,
            label=label,
        )

        propagation_node.additional_properties = d
        return propagation_node

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
