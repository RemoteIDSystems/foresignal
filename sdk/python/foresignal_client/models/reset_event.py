from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.reset_event_reason import ResetEventReason
from ..types import UNSET, Unset

T = TypeVar("T", bound="ResetEvent")


@_attrs_define
class ResetEvent:
    """SSE `reset` event, followed by the server closing the stream: reconnect from `resumeAfter` (or the last received
    id), or re-snapshot when `reason` is `resync_required`

        Attributes:
            reason (ResetEventReason):
            resume_after (int | None | Unset): Last delivered seq to resume from (null for resync_required)
            watermark_seq (int | None | Unset):
    """

    reason: ResetEventReason
    resume_after: int | None | Unset = UNSET
    watermark_seq: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        reason = self.reason.value

        resume_after: int | None | Unset
        if isinstance(self.resume_after, Unset):
            resume_after = UNSET
        else:
            resume_after = self.resume_after

        watermark_seq: int | None | Unset
        if isinstance(self.watermark_seq, Unset):
            watermark_seq = UNSET
        else:
            watermark_seq = self.watermark_seq

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "reason": reason,
            }
        )
        if resume_after is not UNSET:
            field_dict["resumeAfter"] = resume_after
        if watermark_seq is not UNSET:
            field_dict["watermarkSeq"] = watermark_seq

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        reason = ResetEventReason(d.pop("reason"))

        def _parse_resume_after(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        resume_after = _parse_resume_after(d.pop("resumeAfter", UNSET))

        def _parse_watermark_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        watermark_seq = _parse_watermark_seq(d.pop("watermarkSeq", UNSET))

        reset_event = cls(
            reason=reason,
            resume_after=resume_after,
            watermark_seq=watermark_seq,
        )

        reset_event.additional_properties = d
        return reset_event

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
