from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.channel_actions_config_list_item import ChannelActionsConfigListItem
from ..types import UNSET, Unset

T = TypeVar("T", bound="ChannelActionsConfig")


@_attrs_define
class ChannelActionsConfig:
    """Notification action settings inside a channel's `config.actions` (033)

    Attributes:
        enabled (bool | Unset): Default true for ntfy, webpush, telegram, matrix, pushover; false for every other type
            (confirm-page links in the body are an opt-in there)
        list_ (list[ChannelActionsConfigListItem] | Unset):
        links (bool | Unset): Add confirm-page links to the body for channels without buttons Default: True.
        reactions (bool | Unset): Matrix: a ✅ reaction from `allowedSenders` acknowledges Default: False.
    """

    enabled: bool | Unset = UNSET
    list_: list[ChannelActionsConfigListItem] | Unset = UNSET
    links: bool | Unset = True
    reactions: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        list_: list[str] | Unset = UNSET
        if not isinstance(self.list_, Unset):
            list_ = []
            for list_item_data in self.list_:
                list_item = list_item_data.value
                list_.append(list_item)

        links = self.links

        reactions = self.reactions

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if list_ is not UNSET:
            field_dict["list"] = list_
        if links is not UNSET:
            field_dict["links"] = links
        if reactions is not UNSET:
            field_dict["reactions"] = reactions

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        enabled = d.pop("enabled", UNSET)

        _list_ = d.pop("list", UNSET)
        list_: list[ChannelActionsConfigListItem] | Unset = UNSET
        if _list_ is not UNSET:
            list_ = []
            for list_item_data in _list_:
                list_item = ChannelActionsConfigListItem(list_item_data)

                list_.append(list_item)

        links = d.pop("links", UNSET)

        reactions = d.pop("reactions", UNSET)

        channel_actions_config = cls(
            enabled=enabled,
            list_=list_,
            links=links,
            reactions=reactions,
        )

        channel_actions_config.additional_properties = d
        return channel_actions_config

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
