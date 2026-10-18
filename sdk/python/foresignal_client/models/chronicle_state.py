from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.chronicle_state_layers_item import ChronicleStateLayersItem


T = TypeVar("T", bound="ChronicleState")


@_attrs_define
class ChronicleState:
    """State of every layer at time T from chronicle storage (latest captured version per entity with observedAt ≤ T, valid
    by TTL); paged by layer

        Attributes:
            chronicle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            at (datetime.datetime):
            layers (list[ChronicleStateLayersItem]):
            caveats (list[Caveat] | Unset):
    """

    chronicle_id: str
    at: datetime.datetime
    layers: list[ChronicleStateLayersItem]
    caveats: list[Caveat] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        chronicle_id = self.chronicle_id

        at = self.at.isoformat()

        layers = []
        for layers_item_data in self.layers:
            layers_item = layers_item_data.to_dict()
            layers.append(layers_item)

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "chronicleId": chronicle_id,
                "at": at,
                "layers": layers,
            }
        )
        if caveats is not UNSET:
            field_dict["caveats"] = caveats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.chronicle_state_layers_item import ChronicleStateLayersItem

        d = dict(src_dict)
        chronicle_id = d.pop("chronicleId")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        layers = []
        _layers = d.pop("layers")
        for layers_item_data in _layers:
            layers_item = ChronicleStateLayersItem.from_dict(layers_item_data)

            layers.append(layers_item)

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        chronicle_state = cls(
            chronicle_id=chronicle_id,
            at=at,
            layers=layers,
            caveats=caveats,
        )

        chronicle_state.additional_properties = d
        return chronicle_state

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
