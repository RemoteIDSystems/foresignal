from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="ExternalBindingEvent")


@_attrs_define
class ExternalBindingEvent:
    """SSE external.binding.disabled payload

    Attributes:
        binding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        source_id (str):
        reason (str):
        at (datetime.datetime):
    """

    binding_id: str
    source_id: str
    reason: str
    at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        binding_id = self.binding_id

        source_id = self.source_id

        reason = self.reason

        at = self.at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "bindingId": binding_id,
                "sourceId": source_id,
                "reason": reason,
                "at": at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        binding_id = d.pop("bindingId")

        source_id = d.pop("sourceId")

        reason = d.pop("reason")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        external_binding_event = cls(
            binding_id=binding_id,
            source_id=source_id,
            reason=reason,
            at=at,
        )

        external_binding_event.additional_properties = d
        return external_binding_event

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
