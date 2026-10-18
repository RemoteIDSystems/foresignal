from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.regional_index_def_input_components_item import (
        RegionalIndexDefInputComponentsItem,
    )
    from ..models.regional_index_def_input_floors_item import (
        RegionalIndexDefInputFloorsItem,
    )
    from ..models.regional_index_def_input_weights import RegionalIndexDefInputWeights


T = TypeVar("T", bound="RegionalIndexDefInput")


@_attrs_define
class RegionalIndexDefInput:
    """Definition and enablement of a regional index (PUT replaces)

    Attributes:
        name (str):
        components (list[RegionalIndexDefInputComponentsItem]):
        weights (RegionalIndexDefInputWeights): Component key → weight (sums to 1)
        target (str):
        enabled (bool):
        description (None | str | Unset):
        floors (list[RegionalIndexDefInputFloorsItem] | Unset):
        auto_apply_fitted_weights (bool | Unset):  Default: False.
    """

    name: str
    components: list[RegionalIndexDefInputComponentsItem]
    weights: RegionalIndexDefInputWeights
    target: str
    enabled: bool
    description: None | str | Unset = UNSET
    floors: list[RegionalIndexDefInputFloorsItem] | Unset = UNSET
    auto_apply_fitted_weights: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        components = []
        for components_item_data in self.components:
            components_item = components_item_data.to_dict()
            components.append(components_item)

        weights = self.weights.to_dict()

        target = self.target

        enabled = self.enabled

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        floors: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.floors, Unset):
            floors = []
            for floors_item_data in self.floors:
                floors_item = floors_item_data.to_dict()
                floors.append(floors_item)

        auto_apply_fitted_weights = self.auto_apply_fitted_weights

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "components": components,
                "weights": weights,
                "target": target,
                "enabled": enabled,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if floors is not UNSET:
            field_dict["floors"] = floors
        if auto_apply_fitted_weights is not UNSET:
            field_dict["autoApplyFittedWeights"] = auto_apply_fitted_weights

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.regional_index_def_input_components_item import (
            RegionalIndexDefInputComponentsItem,
        )
        from ..models.regional_index_def_input_floors_item import (
            RegionalIndexDefInputFloorsItem,
        )
        from ..models.regional_index_def_input_weights import (
            RegionalIndexDefInputWeights,
        )

        d = dict(src_dict)
        name = d.pop("name")

        components = []
        _components = d.pop("components")
        for components_item_data in _components:
            components_item = RegionalIndexDefInputComponentsItem.from_dict(
                components_item_data
            )

            components.append(components_item)

        weights = RegionalIndexDefInputWeights.from_dict(d.pop("weights"))

        target = d.pop("target")

        enabled = d.pop("enabled")

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        _floors = d.pop("floors", UNSET)
        floors: list[RegionalIndexDefInputFloorsItem] | Unset = UNSET
        if _floors is not UNSET:
            floors = []
            for floors_item_data in _floors:
                floors_item = RegionalIndexDefInputFloorsItem.from_dict(
                    floors_item_data
                )

                floors.append(floors_item)

        auto_apply_fitted_weights = d.pop("autoApplyFittedWeights", UNSET)

        regional_index_def_input = cls(
            name=name,
            components=components,
            weights=weights,
            target=target,
            enabled=enabled,
            description=description,
            floors=floors,
            auto_apply_fitted_weights=auto_apply_fitted_weights,
        )

        regional_index_def_input.additional_properties = d
        return regional_index_def_input

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
