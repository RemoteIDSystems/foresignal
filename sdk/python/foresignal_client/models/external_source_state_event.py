from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.external_source_state_event_change import ExternalSourceStateEventChange
from ..types import UNSET, Unset

T = TypeVar("T", bound="ExternalSourceStateEvent")


@_attrs_define
class ExternalSourceStateEvent:
    """SSE external.source.state payload

    Attributes:
        source_id (str):
        change (ExternalSourceStateEventChange):
        at (datetime.datetime):
        state (str | Unset):
    """

    source_id: str
    change: ExternalSourceStateEventChange
    at: datetime.datetime
    state: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        change = self.change.value

        at = self.at.isoformat()

        state = self.state

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "change": change,
                "at": at,
            }
        )
        if state is not UNSET:
            field_dict["state"] = state

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        change = ExternalSourceStateEventChange(d.pop("change"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        state = d.pop("state", UNSET)

        external_source_state_event = cls(
            source_id=source_id,
            change=change,
            at=at,
            state=state,
        )

        external_source_state_event.additional_properties = d
        return external_source_state_event

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
