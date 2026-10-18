from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="DigestAlertsByStatus")


@_attrs_define
class DigestAlertsByStatus:
    """Counts per lifecycle state (017)

    Attributes:
        firing (int | Unset):
        acknowledged (int | Unset):
        resolved (int | Unset):
    """

    firing: int | Unset = UNSET
    acknowledged: int | Unset = UNSET
    resolved: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        firing = self.firing

        acknowledged = self.acknowledged

        resolved = self.resolved

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if firing is not UNSET:
            field_dict["firing"] = firing
        if acknowledged is not UNSET:
            field_dict["acknowledged"] = acknowledged
        if resolved is not UNSET:
            field_dict["resolved"] = resolved

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        firing = d.pop("firing", UNSET)

        acknowledged = d.pop("acknowledged", UNSET)

        resolved = d.pop("resolved", UNSET)

        digest_alerts_by_status = cls(
            firing=firing,
            acknowledged=acknowledged,
            resolved=resolved,
        )

        digest_alerts_by_status.additional_properties = d
        return digest_alerts_by_status

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
