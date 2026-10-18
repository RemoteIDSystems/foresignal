from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.stats_top_backend import StatsTopBackend
from ..models.stats_top_by import StatsTopBy
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.stats_top_items_item import StatsTopItemsItem


T = TypeVar("T", bound="StatsTop")


@_attrs_define
class StatsTop:
    """Top-N by source, kind, cell or entity (038)

    Attributes:
        by (StatsTopBy):
        measure (str):
        items (list[StatsTopItemsItem]):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        backend (StatsTopBackend | Unset):
    """

    by: StatsTopBy
    measure: str
    items: list[StatsTopItemsItem]
    from_: datetime.datetime | Unset = UNSET
    to: datetime.datetime | Unset = UNSET
    backend: StatsTopBackend | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        by = self.by.value

        measure = self.measure

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        from_: str | Unset = UNSET
        if not isinstance(self.from_, Unset):
            from_ = self.from_.isoformat()

        to: str | Unset = UNSET
        if not isinstance(self.to, Unset):
            to = self.to.isoformat()

        backend: str | Unset = UNSET
        if not isinstance(self.backend, Unset):
            backend = self.backend.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "by": by,
                "measure": measure,
                "items": items,
            }
        )
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if backend is not UNSET:
            field_dict["backend"] = backend

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.stats_top_items_item import StatsTopItemsItem

        d = dict(src_dict)
        by = StatsTopBy(d.pop("by"))

        measure = d.pop("measure")

        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = StatsTopItemsItem.from_dict(items_item_data)

            items.append(items_item)

        _from_ = d.pop("from", UNSET)
        from_: datetime.datetime | Unset
        if isinstance(_from_, Unset):
            from_ = UNSET
        else:
            from_ = datetime.datetime.fromisoformat(_from_)

        _to = d.pop("to", UNSET)
        to: datetime.datetime | Unset
        if isinstance(_to, Unset):
            to = UNSET
        else:
            to = datetime.datetime.fromisoformat(_to)

        _backend = d.pop("backend", UNSET)
        backend: StatsTopBackend | Unset
        if isinstance(_backend, Unset):
            backend = UNSET
        else:
            backend = StatsTopBackend(_backend)

        stats_top = cls(
            by=by,
            measure=measure,
            items=items,
            from_=from_,
            to=to,
            backend=backend,
        )

        stats_top.additional_properties = d
        return stats_top

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
