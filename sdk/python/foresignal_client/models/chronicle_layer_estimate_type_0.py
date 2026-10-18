from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ChronicleLayerEstimateType0")


@_attrs_define
class ChronicleLayerEstimateType0:
    """Projected capture volume of one layer

    Attributes:
        rows_per_day (float):
        bytes_per_day (float):
        basis (None | str | Unset): How the projection was computed, e.g. last 7 d of global history
    """

    rows_per_day: float
    bytes_per_day: float
    basis: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rows_per_day = self.rows_per_day

        bytes_per_day = self.bytes_per_day

        basis: None | str | Unset
        if isinstance(self.basis, Unset):
            basis = UNSET
        else:
            basis = self.basis

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "rowsPerDay": rows_per_day,
                "bytesPerDay": bytes_per_day,
            }
        )
        if basis is not UNSET:
            field_dict["basis"] = basis

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        rows_per_day = d.pop("rowsPerDay")

        bytes_per_day = d.pop("bytesPerDay")

        def _parse_basis(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        basis = _parse_basis(d.pop("basis", UNSET))

        chronicle_layer_estimate_type_0 = cls(
            rows_per_day=rows_per_day,
            bytes_per_day=bytes_per_day,
            basis=basis,
        )

        chronicle_layer_estimate_type_0.additional_properties = d
        return chronicle_layer_estimate_type_0

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
