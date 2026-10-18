from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IngestDeviceSetupTraccar")


@_attrs_define
class IngestDeviceSetupTraccar:
    """Traccar Client / OsmAnd settings

    Attributes:
        server_url (str | Unset): URL with the key in the path
    """

    server_url: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        server_url = self.server_url

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if server_url is not UNSET:
            field_dict["serverUrl"] = server_url

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        server_url = d.pop("serverUrl", UNSET)

        ingest_device_setup_traccar = cls(
            server_url=server_url,
        )

        ingest_device_setup_traccar.additional_properties = d
        return ingest_device_setup_traccar

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
