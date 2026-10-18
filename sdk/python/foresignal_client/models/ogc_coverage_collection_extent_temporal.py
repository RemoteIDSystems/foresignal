from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="OgcCoverageCollectionExtentTemporal")


@_attrs_define
class OgcCoverageCollectionExtentTemporal:
    """Frame times

    Attributes:
        interval (list[list[datetime.datetime | None]] | Unset):
        values (list[str] | Unset): Recent frame times
        trs (str | Unset):
    """

    interval: list[list[datetime.datetime | None]] | Unset = UNSET
    values: list[str] | Unset = UNSET
    trs: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        interval: list[list[None | str]] | Unset = UNSET
        if not isinstance(self.interval, Unset):
            interval = []
            for interval_item_data in self.interval:
                interval_item = []
                for interval_item_item_data in interval_item_data:
                    interval_item_item: None | str
                    if isinstance(interval_item_item_data, datetime.datetime):
                        interval_item_item = interval_item_item_data.isoformat()
                    else:
                        interval_item_item = interval_item_item_data
                    interval_item.append(interval_item_item)

                interval.append(interval_item)

        values: list[str] | Unset = UNSET
        if not isinstance(self.values, Unset):
            values = self.values

        trs = self.trs

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if interval is not UNSET:
            field_dict["interval"] = interval
        if values is not UNSET:
            field_dict["values"] = values
        if trs is not UNSET:
            field_dict["trs"] = trs

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _interval = d.pop("interval", UNSET)
        interval: list[list[datetime.datetime | None]] | Unset = UNSET
        if _interval is not UNSET:
            interval = []
            for interval_item_data in _interval:
                interval_item = []
                _interval_item = interval_item_data
                for interval_item_item_data in _interval_item:

                    def _parse_interval_item_item(
                        data: object,
                    ) -> datetime.datetime | None:
                        if data is None:
                            return data
                        try:
                            if not isinstance(data, str):
                                raise TypeError()
                            interval_item_item_type_0 = datetime.datetime.fromisoformat(
                                data
                            )

                            return interval_item_item_type_0
                        except (TypeError, ValueError, AttributeError, KeyError):
                            pass
                        return cast(datetime.datetime | None, data)

                    interval_item_item = _parse_interval_item_item(
                        interval_item_item_data
                    )

                    interval_item.append(interval_item_item)

                interval.append(interval_item)

        values = cast(list[str], d.pop("values", UNSET))

        trs = d.pop("trs", UNSET)

        ogc_coverage_collection_extent_temporal = cls(
            interval=interval,
            values=values,
            trs=trs,
        )

        ogc_coverage_collection_extent_temporal.additional_properties = d
        return ogc_coverage_collection_extent_temporal

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
