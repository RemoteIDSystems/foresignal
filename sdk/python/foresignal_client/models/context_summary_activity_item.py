from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.context_summary_activity_item_type import ContextSummaryActivityItemType
from ..types import UNSET, Unset

T = TypeVar("T", bound="ContextSummaryActivityItem")


@_attrs_define
class ContextSummaryActivityItem:
    """Activity item, chronological

    Attributes:
        at (datetime.datetime):
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (ContextSummaryActivityItemType):
        title (str):
        severity (None | str | Unset):
        signal_family (None | str | Unset):
    """

    at: datetime.datetime
    ref: str
    type_: ContextSummaryActivityItemType
    title: str
    severity: None | str | Unset = UNSET
    signal_family: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        at = self.at.isoformat()

        ref = self.ref

        type_ = self.type_.value

        title = self.title

        severity: None | str | Unset
        if isinstance(self.severity, Unset):
            severity = UNSET
        else:
            severity = self.severity

        signal_family: None | str | Unset
        if isinstance(self.signal_family, Unset):
            signal_family = UNSET
        else:
            signal_family = self.signal_family

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "at": at,
                "ref": ref,
                "type": type_,
                "title": title,
            }
        )
        if severity is not UNSET:
            field_dict["severity"] = severity
        if signal_family is not UNSET:
            field_dict["signalFamily"] = signal_family

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        at = datetime.datetime.fromisoformat(d.pop("at"))

        ref = d.pop("ref")

        type_ = ContextSummaryActivityItemType(d.pop("type"))

        title = d.pop("title")

        def _parse_severity(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        severity = _parse_severity(d.pop("severity", UNSET))

        def _parse_signal_family(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        signal_family = _parse_signal_family(d.pop("signalFamily", UNSET))

        context_summary_activity_item = cls(
            at=at,
            ref=ref,
            type_=type_,
            title=title,
            severity=severity,
            signal_family=signal_family,
        )

        context_summary_activity_item.additional_properties = d
        return context_summary_activity_item

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
