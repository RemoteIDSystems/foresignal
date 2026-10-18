from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.watchlist_import_result_mode import WatchlistImportResultMode
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.watchlist_import_result_invalid_item import (
        WatchlistImportResultInvalidItem,
    )


T = TypeVar("T", bound="WatchlistImportResult")


@_attrs_define
class WatchlistImportResult:
    """Outcome of adding or importing watchlist items (036); with dryRun nothing changes

    Attributes:
        dry_run (bool):
        added (int):
        updated (int):
        removed (int):
        invalid (list[WatchlistImportResultInvalidItem]):
        item_count (int): Items after the operation (or as it would be)
        mode (WatchlistImportResultMode | Unset):
        unchanged (int | Unset):
    """

    dry_run: bool
    added: int
    updated: int
    removed: int
    invalid: list[WatchlistImportResultInvalidItem]
    item_count: int
    mode: WatchlistImportResultMode | Unset = UNSET
    unchanged: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        dry_run = self.dry_run

        added = self.added

        updated = self.updated

        removed = self.removed

        invalid = []
        for invalid_item_data in self.invalid:
            invalid_item = invalid_item_data.to_dict()
            invalid.append(invalid_item)

        item_count = self.item_count

        mode: str | Unset = UNSET
        if not isinstance(self.mode, Unset):
            mode = self.mode.value

        unchanged = self.unchanged

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "dryRun": dry_run,
                "added": added,
                "updated": updated,
                "removed": removed,
                "invalid": invalid,
                "itemCount": item_count,
            }
        )
        if mode is not UNSET:
            field_dict["mode"] = mode
        if unchanged is not UNSET:
            field_dict["unchanged"] = unchanged

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.watchlist_import_result_invalid_item import (
            WatchlistImportResultInvalidItem,
        )

        d = dict(src_dict)
        dry_run = d.pop("dryRun")

        added = d.pop("added")

        updated = d.pop("updated")

        removed = d.pop("removed")

        invalid = []
        _invalid = d.pop("invalid")
        for invalid_item_data in _invalid:
            invalid_item = WatchlistImportResultInvalidItem.from_dict(invalid_item_data)

            invalid.append(invalid_item)

        item_count = d.pop("itemCount")

        _mode = d.pop("mode", UNSET)
        mode: WatchlistImportResultMode | Unset
        if isinstance(_mode, Unset):
            mode = UNSET
        else:
            mode = WatchlistImportResultMode(_mode)

        unchanged = d.pop("unchanged", UNSET)

        watchlist_import_result = cls(
            dry_run=dry_run,
            added=added,
            updated=updated,
            removed=removed,
            invalid=invalid,
            item_count=item_count,
            mode=mode,
            unchanged=unchanged,
        )

        watchlist_import_result.additional_properties = d
        return watchlist_import_result

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
