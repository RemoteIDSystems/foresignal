from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.record import Record


T = TypeVar("T", bound="RecordPage")


@_attrs_define
class RecordPage:
    """
    Attributes:
        items (list[Record]):
        next_cursor (None | str | Unset):
        watermark_seq (int | Unset): ChangeBus.lastPublishedSeq() read before the query; open SSE with
            after=watermarkSeq
        as_of_seq (int | None | Unset): Echo of the pinned snapshot seq (asOfSeq or order=id)
        as_of (datetime.datetime | None | Unset): Echo of asOf
        approximate (bool | Unset): asOf only: true when a source is SAMPLED/LATEST or the range is downsampled
        horizon (datetime.datetime | None | Unset): asOf only: earliest supported T over the involved sources
        attributions (list[Attribution] | Unset): Aggregated attributions of the records in this page
        licence_filtered (int | Unset): Records removed by the licence policy
    """

    items: list[Record]
    next_cursor: None | str | Unset = UNSET
    watermark_seq: int | Unset = UNSET
    as_of_seq: int | None | Unset = UNSET
    as_of: datetime.datetime | None | Unset = UNSET
    approximate: bool | Unset = UNSET
    horizon: datetime.datetime | None | Unset = UNSET
    attributions: list[Attribution] | Unset = UNSET
    licence_filtered: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        next_cursor: None | str | Unset
        if isinstance(self.next_cursor, Unset):
            next_cursor = UNSET
        else:
            next_cursor = self.next_cursor

        watermark_seq = self.watermark_seq

        as_of_seq: int | None | Unset
        if isinstance(self.as_of_seq, Unset):
            as_of_seq = UNSET
        else:
            as_of_seq = self.as_of_seq

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        approximate = self.approximate

        horizon: None | str | Unset
        if isinstance(self.horizon, Unset):
            horizon = UNSET
        elif isinstance(self.horizon, datetime.datetime):
            horizon = self.horizon.isoformat()
        else:
            horizon = self.horizon

        attributions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.attributions, Unset):
            attributions = []
            for attributions_item_data in self.attributions:
                attributions_item = attributions_item_data.to_dict()
                attributions.append(attributions_item)

        licence_filtered = self.licence_filtered

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "items": items,
            }
        )
        if next_cursor is not UNSET:
            field_dict["nextCursor"] = next_cursor
        if watermark_seq is not UNSET:
            field_dict["watermarkSeq"] = watermark_seq
        if as_of_seq is not UNSET:
            field_dict["asOfSeq"] = as_of_seq
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if approximate is not UNSET:
            field_dict["approximate"] = approximate
        if horizon is not UNSET:
            field_dict["horizon"] = horizon
        if attributions is not UNSET:
            field_dict["attributions"] = attributions
        if licence_filtered is not UNSET:
            field_dict["licenceFiltered"] = licence_filtered

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.record import Record

        d = dict(src_dict)
        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = Record.from_dict(items_item_data)

            items.append(items_item)

        def _parse_next_cursor(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        next_cursor = _parse_next_cursor(d.pop("nextCursor", UNSET))

        watermark_seq = d.pop("watermarkSeq", UNSET)

        def _parse_as_of_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        as_of_seq = _parse_as_of_seq(d.pop("asOfSeq", UNSET))

        def _parse_as_of(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                as_of_type_0 = datetime.datetime.fromisoformat(data)

                return as_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        as_of = _parse_as_of(d.pop("asOf", UNSET))

        approximate = d.pop("approximate", UNSET)

        def _parse_horizon(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                horizon_type_0 = datetime.datetime.fromisoformat(data)

                return horizon_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        horizon = _parse_horizon(d.pop("horizon", UNSET))

        _attributions = d.pop("attributions", UNSET)
        attributions: list[Attribution] | Unset = UNSET
        if _attributions is not UNSET:
            attributions = []
            for attributions_item_data in _attributions:
                attributions_item = Attribution.from_dict(attributions_item_data)

                attributions.append(attributions_item)

        licence_filtered = d.pop("licenceFiltered", UNSET)

        record_page = cls(
            items=items,
            next_cursor=next_cursor,
            watermark_seq=watermark_seq,
            as_of_seq=as_of_seq,
            as_of=as_of,
            approximate=approximate,
            horizon=horizon,
            attributions=attributions,
            licence_filtered=licence_filtered,
        )

        record_page.additional_properties = d
        return record_page

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
