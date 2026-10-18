from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.mesh_channel_common_config_allowed_senders_additional_property import (
    MeshChannelCommonConfigAllowedSendersAdditionalProperty,
)

T = TypeVar("T", bound="MeshChannelCommonConfigAllowedSenders")


@_attrs_define
class MeshChannelCommonConfigAllowedSenders:
    """Sender to role. A sender is a Meshtastic node id (`!xxxxxxxx`), a MeshCore key prefix (`mc:` + 12 hex) or `name:` +
    the advertised name: MeshCore channel messages name their sender only by its advertised name, so a MeshCore channel
    report is matched by `name:` (084)

    """

    additional_properties: dict[
        str, MeshChannelCommonConfigAllowedSendersAdditionalProperty
    ] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        for prop_name, prop in self.additional_properties.items():
            field_dict[prop_name] = prop.value

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        mesh_channel_common_config_allowed_senders = cls()

        additional_properties = {}
        for prop_name, prop_dict in d.items():
            additional_property = (
                MeshChannelCommonConfigAllowedSendersAdditionalProperty(prop_dict)
            )

            additional_properties[prop_name] = additional_property

        mesh_channel_common_config_allowed_senders.additional_properties = (
            additional_properties
        )
        return mesh_channel_common_config_allowed_senders

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(
        self, key: str
    ) -> MeshChannelCommonConfigAllowedSendersAdditionalProperty:
        return self.additional_properties[key]

    def __setitem__(
        self, key: str, value: MeshChannelCommonConfigAllowedSendersAdditionalProperty
    ) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
