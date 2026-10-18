from __future__ import annotations

import datetime
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

from ..types import UNSET, Unset

T = TypeVar("T", bound="AttentionGapEvent")


@_attrs_define
class AttentionGapEvent:
    """SSE `attention.gap` (topic `social`): an `attention-gap` finding (137)

    Attributes:
        event (Literal['attention.gap']):
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        expected (float): Expected posts in the 6 h after the event (normalised)
        observed (float):
        platforms (list[str]):
        at (datetime.datetime):
        region_codes (list[str] | Unset):
    """

    event: Literal["attention.gap"]
    finding_id: str
    ref: str
    expected: float
    observed: float
    platforms: list[str]
    at: datetime.datetime
    region_codes: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event

        finding_id = self.finding_id

        ref = self.ref

        expected = self.expected

        observed = self.observed

        platforms = self.platforms

        at = self.at.isoformat()

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "findingId": finding_id,
                "ref": ref,
                "expected": expected,
                "observed": observed,
                "platforms": platforms,
                "at": at,
            }
        )
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        event = cast(Literal["attention.gap"], d.pop("event"))
        if event != "attention.gap":
            raise ValueError(f"event must match const 'attention.gap', got '{event}'")

        finding_id = d.pop("findingId")

        ref = d.pop("ref")

        expected = d.pop("expected")

        observed = d.pop("observed")

        platforms = cast(list[str], d.pop("platforms"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        attention_gap_event = cls(
            event=event,
            finding_id=finding_id,
            ref=ref,
            expected=expected,
            observed=observed,
            platforms=platforms,
            at=at,
            region_codes=region_codes,
        )

        attention_gap_event.additional_properties = d
        return attention_gap_event

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
