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

from ..models.edp_status_item_health import EdpStatusItemHealth
from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpStatusItem")


@_attrs_define
class EdpStatusItem:
    """Provider health (no record). Without stream it is the source's health; with stream, that stream's health.

    Attributes:
        type_ (Literal['status']):
        health (EdpStatusItemHealth):
        stream (str | Unset):
        message (None | str | Unset):
    """

    type_: Literal["status"]
    health: EdpStatusItemHealth
    stream: str | Unset = UNSET
    message: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        health = self.health.value

        stream = self.stream

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "health": health,
            }
        )
        if stream is not UNSET:
            field_dict["stream"] = stream
        if message is not UNSET:
            field_dict["message"] = message

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = cast(Literal["status"], d.pop("type"))
        if type_ != "status":
            raise ValueError(f"type must match const 'status', got '{type_}'")

        health = EdpStatusItemHealth(d.pop("health"))

        stream = d.pop("stream", UNSET)

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        edp_status_item = cls(
            type_=type_,
            health=health,
            stream=stream,
            message=message,
        )

        edp_status_item.additional_properties = d
        return edp_status_item

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
