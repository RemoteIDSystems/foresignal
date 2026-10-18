from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.credentials_update_values import CredentialsUpdateValues


T = TypeVar("T", bound="CredentialsUpdate")


@_attrs_define
class CredentialsUpdate:
    """
    Attributes:
        values (CredentialsUpdateValues): Keys match the descriptor's credentialFields Example: {'apiKey': '…'}.
    """

    values: CredentialsUpdateValues
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        values = self.values.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "values": values,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.credentials_update_values import CredentialsUpdateValues

        d = dict(src_dict)
        values = CredentialsUpdateValues.from_dict(d.pop("values"))

        credentials_update = cls(
            values=values,
        )

        credentials_update.additional_properties = d
        return credentials_update

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
