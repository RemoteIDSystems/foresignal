from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.timeline_bucket_counts import TimelineBucketCounts


T = TypeVar("T", bound="TimelineBucket")


@_attrs_define
class TimelineBucket:
    """One timeline bucket (037)

    Attributes:
        t (datetime.datetime):
        total (int):
        counts (TimelineBucketCounts | Unset): Per kind or source when groupBy is set
    """

    t: datetime.datetime
    total: int
    counts: TimelineBucketCounts | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        t = self.t.isoformat()

        total = self.total

        counts: dict[str, Any] | Unset = UNSET
        if not isinstance(self.counts, Unset):
            counts = self.counts.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "t": t,
                "total": total,
            }
        )
        if counts is not UNSET:
            field_dict["counts"] = counts

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.timeline_bucket_counts import TimelineBucketCounts

        d = dict(src_dict)
        t = datetime.datetime.fromisoformat(d.pop("t"))

        total = d.pop("total")

        _counts = d.pop("counts", UNSET)
        counts: TimelineBucketCounts | Unset
        if isinstance(_counts, Unset):
            counts = UNSET
        else:
            counts = TimelineBucketCounts.from_dict(_counts)

        timeline_bucket = cls(
            t=t,
            total=total,
            counts=counts,
        )

        timeline_bucket.additional_properties = d
        return timeline_bucket

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
