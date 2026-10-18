from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_volume_scope_type import SocialVolumeScopeType

T = TypeVar("T", bound="SocialVolumeScope")


@_attrs_define
class SocialVolumeScope:
    """What is counted

    Attributes:
        type_ (SocialVolumeScopeType):
        id (str):
    """

    type_: SocialVolumeScopeType
    id: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        id = self.id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "id": id,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = SocialVolumeScopeType(d.pop("type"))

        id = d.pop("id")

        social_volume_scope = cls(
            type_=type_,
            id=id,
        )

        social_volume_scope.additional_properties = d
        return social_volume_scope

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
