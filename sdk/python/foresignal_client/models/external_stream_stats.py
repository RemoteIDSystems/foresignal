from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ExternalStreamStats")


@_attrs_define
class ExternalStreamStats:
    """Per-stream activity

    Attributes:
        stream_id (str | Unset):
        mode (str | Unset):
        kind (str | Unset):
        items (int | Unset):
        rejected (int | Unset):
        last_item_at (datetime.datetime | None | Unset):
        bindings (int | Unset):
    """

    stream_id: str | Unset = UNSET
    mode: str | Unset = UNSET
    kind: str | Unset = UNSET
    items: int | Unset = UNSET
    rejected: int | Unset = UNSET
    last_item_at: datetime.datetime | None | Unset = UNSET
    bindings: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        stream_id = self.stream_id

        mode = self.mode

        kind = self.kind

        items = self.items

        rejected = self.rejected

        last_item_at: None | str | Unset
        if isinstance(self.last_item_at, Unset):
            last_item_at = UNSET
        elif isinstance(self.last_item_at, datetime.datetime):
            last_item_at = self.last_item_at.isoformat()
        else:
            last_item_at = self.last_item_at

        bindings = self.bindings

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if stream_id is not UNSET:
            field_dict["streamId"] = stream_id
        if mode is not UNSET:
            field_dict["mode"] = mode
        if kind is not UNSET:
            field_dict["kind"] = kind
        if items is not UNSET:
            field_dict["items"] = items
        if rejected is not UNSET:
            field_dict["rejected"] = rejected
        if last_item_at is not UNSET:
            field_dict["lastItemAt"] = last_item_at
        if bindings is not UNSET:
            field_dict["bindings"] = bindings

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        stream_id = d.pop("streamId", UNSET)

        mode = d.pop("mode", UNSET)

        kind = d.pop("kind", UNSET)

        items = d.pop("items", UNSET)

        rejected = d.pop("rejected", UNSET)

        def _parse_last_item_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_item_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_item_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_item_at = _parse_last_item_at(d.pop("lastItemAt", UNSET))

        bindings = d.pop("bindings", UNSET)

        external_stream_stats = cls(
            stream_id=stream_id,
            mode=mode,
            kind=kind,
            items=items,
            rejected=rejected,
            last_item_at=last_item_at,
            bindings=bindings,
        )

        external_stream_stats.additional_properties = d
        return external_stream_stats

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
