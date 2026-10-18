from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="UsagePoint")


@_attrs_define
class UsagePoint:
    """
    Attributes:
        t (datetime.datetime):
        requests (int):
        bytes_ (int):
        errors (int):
        refused (int):
        records_seen (int | Unset):
        versions (int | Unset):
        live_updates (int | Unset):
        touches (int | Unset):
        late (int | Unset):
        skipped (int | Unset):
        rejected (int | Unset):
        version_bytes (int | Unset):
    """

    t: datetime.datetime
    requests: int
    bytes_: int
    errors: int
    refused: int
    records_seen: int | Unset = UNSET
    versions: int | Unset = UNSET
    live_updates: int | Unset = UNSET
    touches: int | Unset = UNSET
    late: int | Unset = UNSET
    skipped: int | Unset = UNSET
    rejected: int | Unset = UNSET
    version_bytes: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        t = self.t.isoformat()

        requests = self.requests

        bytes_ = self.bytes_

        errors = self.errors

        refused = self.refused

        records_seen = self.records_seen

        versions = self.versions

        live_updates = self.live_updates

        touches = self.touches

        late = self.late

        skipped = self.skipped

        rejected = self.rejected

        version_bytes = self.version_bytes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "t": t,
                "requests": requests,
                "bytes": bytes_,
                "errors": errors,
                "refused": refused,
            }
        )
        if records_seen is not UNSET:
            field_dict["recordsSeen"] = records_seen
        if versions is not UNSET:
            field_dict["versions"] = versions
        if live_updates is not UNSET:
            field_dict["liveUpdates"] = live_updates
        if touches is not UNSET:
            field_dict["touches"] = touches
        if late is not UNSET:
            field_dict["late"] = late
        if skipped is not UNSET:
            field_dict["skipped"] = skipped
        if rejected is not UNSET:
            field_dict["rejected"] = rejected
        if version_bytes is not UNSET:
            field_dict["versionBytes"] = version_bytes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        t = datetime.datetime.fromisoformat(d.pop("t"))

        requests = d.pop("requests")

        bytes_ = d.pop("bytes")

        errors = d.pop("errors")

        refused = d.pop("refused")

        records_seen = d.pop("recordsSeen", UNSET)

        versions = d.pop("versions", UNSET)

        live_updates = d.pop("liveUpdates", UNSET)

        touches = d.pop("touches", UNSET)

        late = d.pop("late", UNSET)

        skipped = d.pop("skipped", UNSET)

        rejected = d.pop("rejected", UNSET)

        version_bytes = d.pop("versionBytes", UNSET)

        usage_point = cls(
            t=t,
            requests=requests,
            bytes_=bytes_,
            errors=errors,
            refused=refused,
            records_seen=records_seen,
            versions=versions,
            live_updates=live_updates,
            touches=touches,
            late=late,
            skipped=skipped,
            rejected=rejected,
            version_bytes=version_bytes,
        )

        usage_point.additional_properties = d
        return usage_point

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
