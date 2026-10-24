from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.channel_type_id import ChannelTypeId
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.channel_type_capabilities import ChannelTypeCapabilities
    from ..models.channel_type_config_fields_item import ChannelTypeConfigFieldsItem
    from ..models.channel_type_secret_fields_item import ChannelTypeSecretFieldsItem


T = TypeVar("T", bound="ChannelType")


@_attrs_define
class ChannelType:
    """
    Attributes:
        id (ChannelTypeId):
        name (str):
        config_fields (list[ChannelTypeConfigFieldsItem]):
        secret_fields (list[ChannelTypeSecretFieldsItem]): Write-only fields; a name of the form `prefix.<Name>` is a
            wildcard (webhook `header.<Name>` sends any such secret as the header <Name>)
        description (str | Unset):
        events (list[str] | Unset): The events a channel of this type can subscribe to: every `Channel.events` value an
            instance channel accepts
        capabilities (ChannelTypeCapabilities | Unset):
    """

    id: ChannelTypeId
    name: str
    config_fields: list[ChannelTypeConfigFieldsItem]
    secret_fields: list[ChannelTypeSecretFieldsItem]
    description: str | Unset = UNSET
    events: list[str] | Unset = UNSET
    capabilities: ChannelTypeCapabilities | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id.value

        name = self.name

        config_fields = []
        for config_fields_item_data in self.config_fields:
            config_fields_item = config_fields_item_data.to_dict()
            config_fields.append(config_fields_item)

        secret_fields = []
        for secret_fields_item_data in self.secret_fields:
            secret_fields_item = secret_fields_item_data.to_dict()
            secret_fields.append(secret_fields_item)

        description = self.description

        events: list[str] | Unset = UNSET
        if not isinstance(self.events, Unset):
            events = self.events

        capabilities: dict[str, Any] | Unset = UNSET
        if not isinstance(self.capabilities, Unset):
            capabilities = self.capabilities.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "configFields": config_fields,
                "secretFields": secret_fields,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if events is not UNSET:
            field_dict["events"] = events
        if capabilities is not UNSET:
            field_dict["capabilities"] = capabilities

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.channel_type_capabilities import ChannelTypeCapabilities
        from ..models.channel_type_config_fields_item import ChannelTypeConfigFieldsItem
        from ..models.channel_type_secret_fields_item import ChannelTypeSecretFieldsItem

        d = dict(src_dict)
        id = ChannelTypeId(d.pop("id"))

        name = d.pop("name")

        config_fields = []
        _config_fields = d.pop("configFields")
        for config_fields_item_data in _config_fields:
            config_fields_item = ChannelTypeConfigFieldsItem.from_dict(
                config_fields_item_data
            )

            config_fields.append(config_fields_item)

        secret_fields = []
        _secret_fields = d.pop("secretFields")
        for secret_fields_item_data in _secret_fields:
            secret_fields_item = ChannelTypeSecretFieldsItem.from_dict(
                secret_fields_item_data
            )

            secret_fields.append(secret_fields_item)

        description = d.pop("description", UNSET)

        events = cast(list[str], d.pop("events", UNSET))

        _capabilities = d.pop("capabilities", UNSET)
        capabilities: ChannelTypeCapabilities | Unset
        if isinstance(_capabilities, Unset):
            capabilities = UNSET
        else:
            capabilities = ChannelTypeCapabilities.from_dict(_capabilities)

        channel_type = cls(
            id=id,
            name=name,
            config_fields=config_fields,
            secret_fields=secret_fields,
            description=description,
            events=events,
            capabilities=capabilities,
        )

        channel_type.additional_properties = d
        return channel_type

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
