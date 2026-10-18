from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.record_version import RecordVersion


T = TypeVar("T", bound="HistoryPage")


@_attrs_define
class HistoryPage:
    """A page of record versions from /v1/history with licence filtering and attributions (109)

    Attributes:
        items (list[RecordVersion]): Versions ordered by the selected timeField
        next_cursor (None | str | Unset):
        attributions (list[Attribution] | Unset):
        licence_filtered (int | Unset): Records removed by the licence policy
    """

    items: list[RecordVersion]
    next_cursor: None | str | Unset = UNSET
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
        if attributions is not UNSET:
            field_dict["attributions"] = attributions
        if licence_filtered is not UNSET:
            field_dict["licenceFiltered"] = licence_filtered

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.record_version import RecordVersion

        d = dict(src_dict)
        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = RecordVersion.from_dict(items_item_data)

            items.append(items_item)

        def _parse_next_cursor(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        next_cursor = _parse_next_cursor(d.pop("nextCursor", UNSET))

        _attributions = d.pop("attributions", UNSET)
        attributions: list[Attribution] | Unset = UNSET
        if _attributions is not UNSET:
            attributions = []
            for attributions_item_data in _attributions:
                attributions_item = Attribution.from_dict(attributions_item_data)

                attributions.append(attributions_item)

        licence_filtered = d.pop("licenceFiltered", UNSET)

        history_page = cls(
            items=items,
            next_cursor=next_cursor,
            attributions=attributions,
            licence_filtered=licence_filtered,
        )

        history_page.additional_properties = d
        return history_page

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
