from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.chronicle_estimate_layers_item import ChronicleEstimateLayersItem


T = TypeVar("T", bound="ChronicleEstimate")


@_attrs_define
class ChronicleEstimate:
    """Projected storage for a chronicle's current layers

    Attributes:
        chronicle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        computed_at (datetime.datetime):
        layers (list[ChronicleEstimateLayersItem]):
        total_rows_per_day (float):
        total_bytes_per_day (float):
        size_cap_bytes (int):
        bytes_used (int | Unset):
        days_to_cap (float | None | Unset):
        caveats (list[Caveat] | Unset):
    """

    chronicle_id: str
    computed_at: datetime.datetime
    layers: list[ChronicleEstimateLayersItem]
    total_rows_per_day: float
    total_bytes_per_day: float
    size_cap_bytes: int
    bytes_used: int | Unset = UNSET
    days_to_cap: float | None | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        chronicle_id = self.chronicle_id

        computed_at = self.computed_at.isoformat()

        layers = []
        for layers_item_data in self.layers:
            layers_item = layers_item_data.to_dict()
            layers.append(layers_item)

        total_rows_per_day = self.total_rows_per_day

        total_bytes_per_day = self.total_bytes_per_day

        size_cap_bytes = self.size_cap_bytes

        bytes_used = self.bytes_used

        days_to_cap: float | None | Unset
        if isinstance(self.days_to_cap, Unset):
            days_to_cap = UNSET
        else:
            days_to_cap = self.days_to_cap

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
                "computedAt": computed_at,
                "layers": layers,
                "totalRowsPerDay": total_rows_per_day,
                "totalBytesPerDay": total_bytes_per_day,
                "sizeCapBytes": size_cap_bytes,
            }
        )
        if bytes_used is not UNSET:
            field_dict["bytesUsed"] = bytes_used
        if days_to_cap is not UNSET:
            field_dict["daysToCap"] = days_to_cap
        if caveats is not UNSET:
            field_dict["caveats"] = caveats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.chronicle_estimate_layers_item import ChronicleEstimateLayersItem

        d = dict(src_dict)
        chronicle_id = d.pop("chronicleId")

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        layers = []
        _layers = d.pop("layers")
        for layers_item_data in _layers:
            layers_item = ChronicleEstimateLayersItem.from_dict(layers_item_data)

            layers.append(layers_item)

        total_rows_per_day = d.pop("totalRowsPerDay")

        total_bytes_per_day = d.pop("totalBytesPerDay")

        size_cap_bytes = d.pop("sizeCapBytes")

        bytes_used = d.pop("bytesUsed", UNSET)

        def _parse_days_to_cap(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        days_to_cap = _parse_days_to_cap(d.pop("daysToCap", UNSET))

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        chronicle_estimate = cls(
            chronicle_id=chronicle_id,
            computed_at=computed_at,
            layers=layers,
            total_rows_per_day=total_rows_per_day,
            total_bytes_per_day=total_bytes_per_day,
            size_cap_bytes=size_cap_bytes,
            bytes_used=bytes_used,
            days_to_cap=days_to_cap,
            caveats=caveats,
        )

        chronicle_estimate.additional_properties = d
        return chronicle_estimate

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
