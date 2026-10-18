from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.capture_mode import CaptureMode
    from ..models.chronicle_record_version import ChronicleRecordVersion
    from ..models.chronicle_state_layers_item_rollups_item import (
        ChronicleStateLayersItemRollupsItem,
    )


T = TypeVar("T", bound="ChronicleStateLayersItem")


@_attrs_define
class ChronicleStateLayersItem:
    """One layer's state page

    Attributes:
        layer_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        count (int):
        items (list[ChronicleRecordVersion]):
        next_cursor (None | str):
        name (str | Unset):
        capture (CaptureMode | Unset): How a chronicle layer captures matching data
        rollups (list[ChronicleStateLayersItemRollupsItem] | Unset):
    """

    layer_id: str
    count: int
    items: list[ChronicleRecordVersion]
    next_cursor: None | str
    name: str | Unset = UNSET
    capture: CaptureMode | Unset = UNSET
    rollups: list[ChronicleStateLayersItemRollupsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        layer_id = self.layer_id

        count = self.count

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        next_cursor: None | str
        next_cursor = self.next_cursor

        name = self.name

        capture: dict[str, Any] | Unset = UNSET
        if not isinstance(self.capture, Unset):
            capture = self.capture.to_dict()

        rollups: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.rollups, Unset):
            rollups = []
            for rollups_item_data in self.rollups:
                rollups_item = rollups_item_data.to_dict()
                rollups.append(rollups_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "layerId": layer_id,
                "count": count,
                "items": items,
                "nextCursor": next_cursor,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if capture is not UNSET:
            field_dict["capture"] = capture
        if rollups is not UNSET:
            field_dict["rollups"] = rollups

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.capture_mode import CaptureMode
        from ..models.chronicle_record_version import ChronicleRecordVersion
        from ..models.chronicle_state_layers_item_rollups_item import (
            ChronicleStateLayersItemRollupsItem,
        )

        d = dict(src_dict)
        layer_id = d.pop("layerId")

        count = d.pop("count")

        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = ChronicleRecordVersion.from_dict(items_item_data)

            items.append(items_item)

        def _parse_next_cursor(data: object) -> None | str:
            if data is None:
                return data
            return cast(None | str, data)

        next_cursor = _parse_next_cursor(d.pop("nextCursor"))

        name = d.pop("name", UNSET)

        _capture = d.pop("capture", UNSET)
        capture: CaptureMode | Unset
        if isinstance(_capture, Unset):
            capture = UNSET
        else:
            capture = CaptureMode.from_dict(_capture)

        _rollups = d.pop("rollups", UNSET)
        rollups: list[ChronicleStateLayersItemRollupsItem] | Unset = UNSET
        if _rollups is not UNSET:
            rollups = []
            for rollups_item_data in _rollups:
                rollups_item = ChronicleStateLayersItemRollupsItem.from_dict(
                    rollups_item_data
                )

                rollups.append(rollups_item)

        chronicle_state_layers_item = cls(
            layer_id=layer_id,
            count=count,
            items=items,
            next_cursor=next_cursor,
            name=name,
            capture=capture,
            rollups=rollups,
        )

        chronicle_state_layers_item.additional_properties = d
        return chronicle_state_layers_item

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
