from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IngestDeviceSetupGpslogger")


@_attrs_define
class IngestDeviceSetupGpslogger:
    """GPSLogger custom URL

    Attributes:
        url_template (str | Unset):
        method (str | Unset):
    """

    url_template: str | Unset = UNSET
    method: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        url_template = self.url_template

        method = self.method

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if url_template is not UNSET:
            field_dict["urlTemplate"] = url_template
        if method is not UNSET:
            field_dict["method"] = method

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        url_template = d.pop("urlTemplate", UNSET)

        method = d.pop("method", UNSET)

        ingest_device_setup_gpslogger = cls(
            url_template=url_template,
            method=method,
        )

        ingest_device_setup_gpslogger.additional_properties = d
        return ingest_device_setup_gpslogger

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
