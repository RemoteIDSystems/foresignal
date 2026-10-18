from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CustomSourceFormatsCap")


@_attrs_define
class CustomSourceFormatsCap:
    """CAP 1.2 options

    Attributes:
        accept_non_actual (bool | Unset):  Default: False.
    """

    accept_non_actual: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        accept_non_actual = self.accept_non_actual

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if accept_non_actual is not UNSET:
            field_dict["acceptNonActual"] = accept_non_actual

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        accept_non_actual = d.pop("acceptNonActual", UNSET)

        custom_source_formats_cap = cls(
            accept_non_actual=accept_non_actual,
        )

        custom_source_formats_cap.additional_properties = d
        return custom_source_formats_cap

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
