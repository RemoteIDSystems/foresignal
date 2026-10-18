from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialCoverageBucket")


@_attrs_define
class SocialCoverageBucket:
    """One hour of collection coverage

    Attributes:
        hour (datetime.datetime):
        posts_seen (int): Posts attributed to the region (platform geo, 136 text-derived places)
        complete (bool): Complete-stream collection was healthy for the whole hour
        denominator (int | None | Unset): All posts seen on complete streams for the platform
    """

    hour: datetime.datetime
    posts_seen: int
    complete: bool
    denominator: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        hour = self.hour.isoformat()

        posts_seen = self.posts_seen

        complete = self.complete

        denominator: int | None | Unset
        if isinstance(self.denominator, Unset):
            denominator = UNSET
        else:
            denominator = self.denominator

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "hour": hour,
                "postsSeen": posts_seen,
                "complete": complete,
            }
        )
        if denominator is not UNSET:
            field_dict["denominator"] = denominator

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        hour = datetime.datetime.fromisoformat(d.pop("hour"))

        posts_seen = d.pop("postsSeen")

        complete = d.pop("complete")

        def _parse_denominator(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        denominator = _parse_denominator(d.pop("denominator", UNSET))

        social_coverage_bucket = cls(
            hour=hour,
            posts_seen=posts_seen,
            complete=complete,
            denominator=denominator,
        )

        social_coverage_bucket.additional_properties = d
        return social_coverage_bucket

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
