from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.custom_source_formats_cloudevents_type_kinds import (
        CustomSourceFormatsCloudeventsTypeKinds,
    )


T = TypeVar("T", bound="CustomSourceFormatsCloudevents")


@_attrs_define
class CustomSourceFormatsCloudevents:
    """CloudEvents options

    Attributes:
        type_kinds (CustomSourceFormatsCloudeventsTypeKinds | Unset): CloudEvents `type` → kind; else the single kind,
            else rejected
    """

    type_kinds: CustomSourceFormatsCloudeventsTypeKinds | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_kinds: dict[str, Any] | Unset = UNSET
        if not isinstance(self.type_kinds, Unset):
            type_kinds = self.type_kinds.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if type_kinds is not UNSET:
            field_dict["typeKinds"] = type_kinds

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.custom_source_formats_cloudevents_type_kinds import (
            CustomSourceFormatsCloudeventsTypeKinds,
        )

        d = dict(src_dict)
        _type_kinds = d.pop("typeKinds", UNSET)
        type_kinds: CustomSourceFormatsCloudeventsTypeKinds | Unset
        if isinstance(_type_kinds, Unset):
            type_kinds = UNSET
        else:
            type_kinds = CustomSourceFormatsCloudeventsTypeKinds.from_dict(_type_kinds)

        custom_source_formats_cloudevents = cls(
            type_kinds=type_kinds,
        )

        custom_source_formats_cloudevents.additional_properties = d
        return custom_source_formats_cloudevents

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
