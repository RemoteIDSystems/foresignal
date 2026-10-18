from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="FcmProviderConfig")


@_attrs_define
class FcmProviderConfig:
    """FCM HTTP v1 provider configuration (032). projectId and clientEmail may be omitted when the service-account JSON
    supplies them (project_id, client_email).

        Attributes:
            project_id (str):
            client_email (str):
            token_uri (str | Unset): OAuth 2.0 token endpoint (https; http is accepted only for a local stand-in) Default:
                'https://oauth2.googleapis.com/token'.
    """

    project_id: str
    client_email: str
    token_uri: str | Unset = "https://oauth2.googleapis.com/token"
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        project_id = self.project_id

        client_email = self.client_email

        token_uri = self.token_uri

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "projectId": project_id,
                "clientEmail": client_email,
            }
        )
        if token_uri is not UNSET:
            field_dict["tokenUri"] = token_uri

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        project_id = d.pop("projectId")

        client_email = d.pop("clientEmail")

        token_uri = d.pop("tokenUri", UNSET)

        fcm_provider_config = cls(
            project_id=project_id,
            client_email=client_email,
            token_uri=token_uri,
        )

        fcm_provider_config.additional_properties = d
        return fcm_provider_config

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
