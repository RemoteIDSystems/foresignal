from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.detectability import Detectability


T = TypeVar("T", bound="ObservabilityCoverage")


@_attrs_define
class ObservabilityCoverage:
    """Coverage layer response: detectability per region or H3 cell (116)

    Attributes:
        family (str):
        at (datetime.datetime):
        absence_min_detect (float):
        items (list[Detectability]):
        band (None | str | Unset):
        next_cursor (None | str | Unset):
    """

    family: str
    at: datetime.datetime
    absence_min_detect: float
    items: list[Detectability]
    band: None | str | Unset = UNSET
    next_cursor: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        family = self.family

        at = self.at.isoformat()

        absence_min_detect = self.absence_min_detect

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        band: None | str | Unset
        if isinstance(self.band, Unset):
            band = UNSET
        else:
            band = self.band

        next_cursor: None | str | Unset
        if isinstance(self.next_cursor, Unset):
            next_cursor = UNSET
        else:
            next_cursor = self.next_cursor

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "family": family,
                "at": at,
                "absenceMinDetect": absence_min_detect,
                "items": items,
            }
        )
        if band is not UNSET:
            field_dict["band"] = band
        if next_cursor is not UNSET:
            field_dict["nextCursor"] = next_cursor

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.detectability import Detectability

        d = dict(src_dict)
        family = d.pop("family")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        absence_min_detect = d.pop("absenceMinDetect")

        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = Detectability.from_dict(items_item_data)

            items.append(items_item)

        def _parse_band(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        band = _parse_band(d.pop("band", UNSET))

        def _parse_next_cursor(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        next_cursor = _parse_next_cursor(d.pop("nextCursor", UNSET))

        observability_coverage = cls(
            family=family,
            at=at,
            absence_min_detect=absence_min_detect,
            items=items,
            band=band,
            next_cursor=next_cursor,
        )

        observability_coverage.additional_properties = d
        return observability_coverage

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
