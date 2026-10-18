from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.cycle_packet_item_detail_type_0 import CyclePacketItemDetailType0


T = TypeVar("T", bound="CyclePacketItem")


@_attrs_define
class CyclePacketItem:
    """One packet entry: an object and what happened to it

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        title (str):
        change (str):  Example: escalated.
        severity (None | str | Unset):
        at (datetime.datetime | None | Unset):
        detail (CyclePacketItemDetailType0 | None | Unset):
        rank (int | None | Unset):
    """

    ref: str
    title: str
    change: str
    severity: None | str | Unset = UNSET
    at: datetime.datetime | None | Unset = UNSET
    detail: CyclePacketItemDetailType0 | None | Unset = UNSET
    rank: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.cycle_packet_item_detail_type_0 import CyclePacketItemDetailType0

        ref = self.ref

        title = self.title

        change = self.change

        severity: None | str | Unset
        if isinstance(self.severity, Unset):
            severity = UNSET
        else:
            severity = self.severity

        at: None | str | Unset
        if isinstance(self.at, Unset):
            at = UNSET
        elif isinstance(self.at, datetime.datetime):
            at = self.at.isoformat()
        else:
            at = self.at

        detail: dict[str, Any] | None | Unset
        if isinstance(self.detail, Unset):
            detail = UNSET
        elif isinstance(self.detail, CyclePacketItemDetailType0):
            detail = self.detail.to_dict()
        else:
            detail = self.detail

        rank: int | None | Unset
        if isinstance(self.rank, Unset):
            rank = UNSET
        else:
            rank = self.rank

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "title": title,
                "change": change,
            }
        )
        if severity is not UNSET:
            field_dict["severity"] = severity
        if at is not UNSET:
            field_dict["at"] = at
        if detail is not UNSET:
            field_dict["detail"] = detail
        if rank is not UNSET:
            field_dict["rank"] = rank

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.cycle_packet_item_detail_type_0 import CyclePacketItemDetailType0

        d = dict(src_dict)
        ref = d.pop("ref")

        title = d.pop("title")

        change = d.pop("change")

        def _parse_severity(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        severity = _parse_severity(d.pop("severity", UNSET))

        def _parse_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                at_type_0 = datetime.datetime.fromisoformat(data)

                return at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        at = _parse_at(d.pop("at", UNSET))

        def _parse_detail(data: object) -> CyclePacketItemDetailType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                detail_type_0 = CyclePacketItemDetailType0.from_dict(data)

                return detail_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(CyclePacketItemDetailType0 | None | Unset, data)

        detail = _parse_detail(d.pop("detail", UNSET))

        def _parse_rank(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        rank = _parse_rank(d.pop("rank", UNSET))

        cycle_packet_item = cls(
            ref=ref,
            title=title,
            change=change,
            severity=severity,
            at=at,
            detail=detail,
            rank=rank,
        )

        cycle_packet_item.additional_properties = d
        return cycle_packet_item

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
