from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.window_state_list_type import WindowStateListType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.window_state import WindowState


T = TypeVar("T", bound="WindowStateList")


@_attrs_define
class WindowStateList:
    """Window state of every group of one rule

    Attributes:
        rule_id (UUID):
        type_ (WindowStateListType):
        groups (list[WindowState]):
        groups_truncated (bool | Unset):
    """

    rule_id: UUID
    type_: WindowStateListType
    groups: list[WindowState]
    groups_truncated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rule_id = str(self.rule_id)

        type_ = self.type_.value

        groups = []
        for groups_item_data in self.groups:
            groups_item = groups_item_data.to_dict()
            groups.append(groups_item)

        groups_truncated = self.groups_truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ruleId": rule_id,
                "type": type_,
                "groups": groups,
            }
        )
        if groups_truncated is not UNSET:
            field_dict["groupsTruncated"] = groups_truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.window_state import WindowState

        d = dict(src_dict)
        rule_id = UUID(d.pop("ruleId"))

        type_ = WindowStateListType(d.pop("type"))

        groups = []
        _groups = d.pop("groups")
        for groups_item_data in _groups:
            groups_item = WindowState.from_dict(groups_item_data)

            groups.append(groups_item)

        groups_truncated = d.pop("groupsTruncated", UNSET)

        window_state_list = cls(
            rule_id=rule_id,
            type_=type_,
            groups=groups,
            groups_truncated=groups_truncated,
        )

        window_state_list.additional_properties = d
        return window_state_list

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
