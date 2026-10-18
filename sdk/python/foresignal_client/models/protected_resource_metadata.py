from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.protected_resource_metadata_bearer_methods_supported_item import (
    ProtectedResourceMetadataBearerMethodsSupportedItem,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ProtectedResourceMetadata")


@_attrs_define
class ProtectedResourceMetadata:
    """OAuth 2.0 Protected Resource Metadata (RFC 9728) for `/mcp`; member names follow the RFC (snake_case)

    Attributes:
        resource (str): Canonical resource URI, default `{publicBaseUrl}/mcp`
        authorization_servers (list[str]): The identity provider's issuer (`mcp.auth.issuer`); empty while none is
            configured
        bearer_methods_supported (list[ProtectedResourceMetadataBearerMethodsSupportedItem]):
        scopes_supported (list[str] | Unset): The identity-provider scopes of `mcp.auth.scopeMap` that map to `read` or
            `agent`, sorted Example: ['foresignal:read', 'foresignal:agent'].
        resource_name (Literal['Foresignal'] | Unset):
        resource_documentation (None | str | Unset): The API documentation, `{base}/docs`
    """

    resource: str
    authorization_servers: list[str]
    bearer_methods_supported: list[ProtectedResourceMetadataBearerMethodsSupportedItem]
    scopes_supported: list[str] | Unset = UNSET
    resource_name: Literal["Foresignal"] | Unset = UNSET
    resource_documentation: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        resource = self.resource

        authorization_servers = self.authorization_servers

        bearer_methods_supported = []
        for bearer_methods_supported_item_data in self.bearer_methods_supported:
            bearer_methods_supported_item = bearer_methods_supported_item_data.value
            bearer_methods_supported.append(bearer_methods_supported_item)

        scopes_supported: list[str] | Unset = UNSET
        if not isinstance(self.scopes_supported, Unset):
            scopes_supported = self.scopes_supported

        resource_name = self.resource_name

        resource_documentation: None | str | Unset
        if isinstance(self.resource_documentation, Unset):
            resource_documentation = UNSET
        else:
            resource_documentation = self.resource_documentation

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "resource": resource,
                "authorization_servers": authorization_servers,
                "bearer_methods_supported": bearer_methods_supported,
            }
        )
        if scopes_supported is not UNSET:
            field_dict["scopes_supported"] = scopes_supported
        if resource_name is not UNSET:
            field_dict["resource_name"] = resource_name
        if resource_documentation is not UNSET:
            field_dict["resource_documentation"] = resource_documentation

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        resource = d.pop("resource")

        authorization_servers = cast(list[str], d.pop("authorization_servers"))

        bearer_methods_supported = []
        _bearer_methods_supported = d.pop("bearer_methods_supported")
        for bearer_methods_supported_item_data in _bearer_methods_supported:
            bearer_methods_supported_item = (
                ProtectedResourceMetadataBearerMethodsSupportedItem(
                    bearer_methods_supported_item_data
                )
            )

            bearer_methods_supported.append(bearer_methods_supported_item)

        scopes_supported = cast(list[str], d.pop("scopes_supported", UNSET))

        resource_name = cast(
            Literal["Foresignal"] | Unset, d.pop("resource_name", UNSET)
        )
        if resource_name != "Foresignal" and not isinstance(resource_name, Unset):
            raise ValueError(
                f"resource_name must match const 'Foresignal', got '{resource_name}'"
            )

        def _parse_resource_documentation(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        resource_documentation = _parse_resource_documentation(
            d.pop("resource_documentation", UNSET)
        )

        protected_resource_metadata = cls(
            resource=resource,
            authorization_servers=authorization_servers,
            bearer_methods_supported=bearer_methods_supported,
            scopes_supported=scopes_supported,
            resource_name=resource_name,
            resource_documentation=resource_documentation,
        )

        protected_resource_metadata.additional_properties = d
        return protected_resource_metadata

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
