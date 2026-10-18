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

from ..models.media_reuse_event_reason import MediaReuseEventReason
from ..types import UNSET, Unset

T = TypeVar("T", bound="MediaReuseEvent")


@_attrs_define
class MediaReuseEvent:
    """SSE `media.reuse` (topic `social`): a `media-reuse` finding (136)

    Attributes:
        event (Literal['media.reuse']):
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        fingerprint_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        first_seen_at (datetime.datetime):
        reason (MediaReuseEventReason): First seen ≥ 7 days earlier, or attached to an earlier narrative in a different
            place
        at (datetime.datetime):
        earlier_narrative_id (None | str | Unset):
    """

    event: Literal["media.reuse"]
    finding_id: str
    fingerprint_id: str
    narrative_id: str
    first_seen_at: datetime.datetime
    reason: MediaReuseEventReason
    at: datetime.datetime
    earlier_narrative_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event

        finding_id = self.finding_id

        fingerprint_id = self.fingerprint_id

        narrative_id = self.narrative_id

        first_seen_at = self.first_seen_at.isoformat()

        reason = self.reason.value

        at = self.at.isoformat()

        earlier_narrative_id: None | str | Unset
        if isinstance(self.earlier_narrative_id, Unset):
            earlier_narrative_id = UNSET
        else:
            earlier_narrative_id = self.earlier_narrative_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "findingId": finding_id,
                "fingerprintId": fingerprint_id,
                "narrativeId": narrative_id,
                "firstSeenAt": first_seen_at,
                "reason": reason,
                "at": at,
            }
        )
        if earlier_narrative_id is not UNSET:
            field_dict["earlierNarrativeId"] = earlier_narrative_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        event = cast(Literal["media.reuse"], d.pop("event"))
        if event != "media.reuse":
            raise ValueError(f"event must match const 'media.reuse', got '{event}'")

        finding_id = d.pop("findingId")

        fingerprint_id = d.pop("fingerprintId")

        narrative_id = d.pop("narrativeId")

        first_seen_at = datetime.datetime.fromisoformat(d.pop("firstSeenAt"))

        reason = MediaReuseEventReason(d.pop("reason"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_earlier_narrative_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        earlier_narrative_id = _parse_earlier_narrative_id(
            d.pop("earlierNarrativeId", UNSET)
        )

        media_reuse_event = cls(
            event=event,
            finding_id=finding_id,
            fingerprint_id=fingerprint_id,
            narrative_id=narrative_id,
            first_seen_at=first_seen_at,
            reason=reason,
            at=at,
            earlier_narrative_id=earlier_narrative_id,
        )

        media_reuse_event.additional_properties = d
        return media_reuse_event

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
