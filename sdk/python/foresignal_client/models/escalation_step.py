from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EscalationStep")


@_attrs_define
class EscalationStep:
    """One escalation step (033)

    Attributes:
        after_minutes (int): Strictly increasing across steps
        channel_ids (list[UUID] | Unset):
        include_push (bool | Unset):  Default: False.
    """

    after_minutes: int
    channel_ids: list[UUID] | Unset = UNSET
    include_push: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        after_minutes = self.after_minutes

        channel_ids: list[str] | Unset = UNSET
        if not isinstance(self.channel_ids, Unset):
            channel_ids = []
            for channel_ids_item_data in self.channel_ids:
                channel_ids_item = str(channel_ids_item_data)
                channel_ids.append(channel_ids_item)

        include_push = self.include_push

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "afterMinutes": after_minutes,
            }
        )
        if channel_ids is not UNSET:
            field_dict["channelIds"] = channel_ids
        if include_push is not UNSET:
            field_dict["includePush"] = include_push

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        after_minutes = d.pop("afterMinutes")

        _channel_ids = d.pop("channelIds", UNSET)
        channel_ids: list[UUID] | Unset = UNSET
        if _channel_ids is not UNSET:
            channel_ids = []
            for channel_ids_item_data in _channel_ids:
                channel_ids_item = UUID(channel_ids_item_data)

                channel_ids.append(channel_ids_item)

        include_push = d.pop("includePush", UNSET)

        escalation_step = cls(
            after_minutes=after_minutes,
            channel_ids=channel_ids,
            include_push=include_push,
        )

        escalation_step.additional_properties = d
        return escalation_step

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
