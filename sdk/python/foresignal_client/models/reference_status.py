from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.reference_dataset import ReferenceDataset


T = TypeVar("T", bound="ReferenceStatus")


@_attrs_define
class ReferenceStatus:
    """Reference data state (040)

    Attributes:
        datasets (list[ReferenceDataset]):
        loading (bool | Unset):
        attributions (list[str] | Unset):
        boundary_note (str | Unset): Natural Earth draws de facto boundaries
    """

    datasets: list[ReferenceDataset]
    loading: bool | Unset = UNSET
    attributions: list[str] | Unset = UNSET
    boundary_note: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        datasets = []
        for datasets_item_data in self.datasets:
            datasets_item = datasets_item_data.to_dict()
            datasets.append(datasets_item)

        loading = self.loading

        attributions: list[str] | Unset = UNSET
        if not isinstance(self.attributions, Unset):
            attributions = self.attributions

        boundary_note = self.boundary_note

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "datasets": datasets,
            }
        )
        if loading is not UNSET:
            field_dict["loading"] = loading
        if attributions is not UNSET:
            field_dict["attributions"] = attributions
        if boundary_note is not UNSET:
            field_dict["boundaryNote"] = boundary_note

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.reference_dataset import ReferenceDataset

        d = dict(src_dict)
        datasets = []
        _datasets = d.pop("datasets")
        for datasets_item_data in _datasets:
            datasets_item = ReferenceDataset.from_dict(datasets_item_data)

            datasets.append(datasets_item)

        loading = d.pop("loading", UNSET)

        attributions = cast(list[str], d.pop("attributions", UNSET))

        boundary_note = d.pop("boundaryNote", UNSET)

        reference_status = cls(
            datasets=datasets,
            loading=loading,
            attributions=attributions,
            boundary_note=boundary_note,
        )

        reference_status.additional_properties = d
        return reference_status

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
