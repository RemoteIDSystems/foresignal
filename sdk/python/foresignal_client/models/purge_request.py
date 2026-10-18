from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PurgeRequest")


@_attrs_define
class PurgeRequest:
    """Options for an entity purge (039)

    Attributes:
        purge_alerts (bool | Unset): Delete referencing alerts instead of redacting them Default: False.
    """

    purge_alerts: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        purge_alerts = self.purge_alerts

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if purge_alerts is not UNSET:
            field_dict["purgeAlerts"] = purge_alerts

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        purge_alerts = d.pop("purgeAlerts", UNSET)

        purge_request = cls(
            purge_alerts=purge_alerts,
        )

        purge_request.additional_properties = d
        return purge_request

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
