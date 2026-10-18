from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.capture_mode import CaptureMode


T = TypeVar("T", bound="ChronicleEstimateLayersItem")


@_attrs_define
class ChronicleEstimateLayersItem:
    """Per-layer projection

    Attributes:
        layer_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        rows_per_day (float):
        bytes_per_day (float):
        name (str | Unset):
        capture (CaptureMode | Unset): How a chronicle layer captures matching data
        basis (None | str | Unset):
    """

    layer_id: str
    rows_per_day: float
    bytes_per_day: float
    name: str | Unset = UNSET
    capture: CaptureMode | Unset = UNSET
    basis: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        layer_id = self.layer_id

        rows_per_day = self.rows_per_day

        bytes_per_day = self.bytes_per_day

        name = self.name

        capture: dict[str, Any] | Unset = UNSET
        if not isinstance(self.capture, Unset):
            capture = self.capture.to_dict()

        basis: None | str | Unset
        if isinstance(self.basis, Unset):
            basis = UNSET
        else:
            basis = self.basis

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "layerId": layer_id,
                "rowsPerDay": rows_per_day,
                "bytesPerDay": bytes_per_day,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if capture is not UNSET:
            field_dict["capture"] = capture
        if basis is not UNSET:
            field_dict["basis"] = basis

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.capture_mode import CaptureMode

        d = dict(src_dict)
        layer_id = d.pop("layerId")

        rows_per_day = d.pop("rowsPerDay")

        bytes_per_day = d.pop("bytesPerDay")

        name = d.pop("name", UNSET)

        _capture = d.pop("capture", UNSET)
        capture: CaptureMode | Unset
        if isinstance(_capture, Unset):
            capture = UNSET
        else:
            capture = CaptureMode.from_dict(_capture)

        def _parse_basis(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        basis = _parse_basis(d.pop("basis", UNSET))

        chronicle_estimate_layers_item = cls(
            layer_id=layer_id,
            rows_per_day=rows_per_day,
            bytes_per_day=bytes_per_day,
            name=name,
            capture=capture,
            basis=basis,
        )

        chronicle_estimate_layers_item.additional_properties = d
        return chronicle_estimate_layers_item

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
