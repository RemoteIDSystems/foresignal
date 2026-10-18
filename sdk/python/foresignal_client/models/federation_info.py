from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.federation_info_capabilities_item import FederationInfoCapabilitiesItem
from ..types import UNSET, Unset

T = TypeVar("T", bound="FederationInfo")


@_attrs_define
class FederationInfo:
    """Instance identity for federation; the response carries a Foresignal-Signature JWS over the body (113)

    Attributes:
        instance_id (str): Install id (003)
        version (str):
        api_version (str):
        jwks_uri (str):
        capabilities (list[FederationInfoCapabilitiesItem]):
        time (datetime.datetime):
        instance_name (None | str | Unset):
    """

    instance_id: str
    version: str
    api_version: str
    jwks_uri: str
    capabilities: list[FederationInfoCapabilitiesItem]
    time: datetime.datetime
    instance_name: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        instance_id = self.instance_id

        version = self.version

        api_version = self.api_version

        jwks_uri = self.jwks_uri

        capabilities = []
        for capabilities_item_data in self.capabilities:
            capabilities_item = capabilities_item_data.value
            capabilities.append(capabilities_item)

        time = self.time.isoformat()

        instance_name: None | str | Unset
        if isinstance(self.instance_name, Unset):
            instance_name = UNSET
        else:
            instance_name = self.instance_name

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "instanceId": instance_id,
                "version": version,
                "apiVersion": api_version,
                "jwksUri": jwks_uri,
                "capabilities": capabilities,
                "time": time,
            }
        )
        if instance_name is not UNSET:
            field_dict["instanceName"] = instance_name

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        instance_id = d.pop("instanceId")

        version = d.pop("version")

        api_version = d.pop("apiVersion")

        jwks_uri = d.pop("jwksUri")

        capabilities = []
        _capabilities = d.pop("capabilities")
        for capabilities_item_data in _capabilities:
            capabilities_item = FederationInfoCapabilitiesItem(capabilities_item_data)

            capabilities.append(capabilities_item)

        time = datetime.datetime.fromisoformat(d.pop("time"))

        def _parse_instance_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        instance_name = _parse_instance_name(d.pop("instanceName", UNSET))

        federation_info = cls(
            instance_id=instance_id,
            version=version,
            api_version=api_version,
            jwks_uri=jwks_uri,
            capabilities=capabilities,
            time=time,
            instance_name=instance_name,
        )

        federation_info.additional_properties = d
        return federation_info

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
