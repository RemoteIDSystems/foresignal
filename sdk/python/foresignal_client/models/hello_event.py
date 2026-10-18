from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="HelloEvent")


@_attrs_define
class HelloEvent:
    """First event on /v1/events/stream: stream id for filter PATCHes, subscribed topics and the change watermark

    Attributes:
        stream_id (str): 129 random bits, base64url; bound to the caller's principal
        topics (list[str]): Subscribed topics
        watermark_seq (int):
        filter_version (int): Starts at 1; incremented by each applied filter PATCH
        stream_key (None | str | Unset): Anonymous callers only: send as `Foresignal-Stream-Key` with filter PATCHes
    """

    stream_id: str
    topics: list[str]
    watermark_seq: int
    filter_version: int
    stream_key: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        stream_id = self.stream_id

        topics = self.topics

        watermark_seq = self.watermark_seq

        filter_version = self.filter_version

        stream_key: None | str | Unset
        if isinstance(self.stream_key, Unset):
            stream_key = UNSET
        else:
            stream_key = self.stream_key

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "streamId": stream_id,
                "topics": topics,
                "watermarkSeq": watermark_seq,
                "filterVersion": filter_version,
            }
        )
        if stream_key is not UNSET:
            field_dict["streamKey"] = stream_key

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        stream_id = d.pop("streamId")

        topics = cast(list[str], d.pop("topics"))

        watermark_seq = d.pop("watermarkSeq")

        filter_version = d.pop("filterVersion")

        def _parse_stream_key(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        stream_key = _parse_stream_key(d.pop("streamKey", UNSET))

        hello_event = cls(
            stream_id=stream_id,
            topics=topics,
            watermark_seq=watermark_seq,
            filter_version=filter_version,
            stream_key=stream_key,
        )

        hello_event.additional_properties = d
        return hello_event

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
