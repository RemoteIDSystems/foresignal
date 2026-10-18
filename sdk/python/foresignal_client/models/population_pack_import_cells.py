from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PopulationPackImportCells")


@_attrs_define
class PopulationPackImportCells:
    """Imported H3 cells

    Attributes:
        res5 (int | Unset):
        res7 (int | Unset):
    """

    res5: int | Unset = UNSET
    res7: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        res5 = self.res5

        res7 = self.res7

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if res5 is not UNSET:
            field_dict["res5"] = res5
        if res7 is not UNSET:
            field_dict["res7"] = res7

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        res5 = d.pop("res5", UNSET)

        res7 = d.pop("res7", UNSET)

        population_pack_import_cells = cls(
            res5=res5,
            res7=res7,
        )

        population_pack_import_cells.additional_properties = d
        return population_pack_import_cells

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
