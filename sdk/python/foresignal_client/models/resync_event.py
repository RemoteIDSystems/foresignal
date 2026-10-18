from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.resync_event_reason import ResyncEventReason

T = TypeVar("T", bound="ResyncEvent")


@_attrs_define
class ResyncEvent:
    """SSE `resync` event: live updates were lost; re-fetch current state for the view (`/v1/records?order=id` or tiles)
    and keep the stream open

        Attributes:
            reason (ResyncEventReason):
            watermark_seq (int):
    """

    reason: ResyncEventReason
    watermark_seq: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        reason = self.reason.value

        watermark_seq = self.watermark_seq

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "reason": reason,
                "watermarkSeq": watermark_seq,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        reason = ResyncEventReason(d.pop("reason"))

        watermark_seq = d.pop("watermarkSeq")

        resync_event = cls(
            reason=reason,
            watermark_seq=watermark_seq,
        )

        resync_event.additional_properties = d
        return resync_event

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
