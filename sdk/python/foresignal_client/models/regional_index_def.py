from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.regional_index_def_weights_origin import RegionalIndexDefWeightsOrigin
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.regional_index_def_components_item import (
        RegionalIndexDefComponentsItem,
    )
    from ..models.regional_index_def_floors_item import RegionalIndexDefFloorsItem
    from ..models.regional_index_def_skill_type_0 import RegionalIndexDefSkillType0
    from ..models.regional_index_def_weights import RegionalIndexDefWeights


T = TypeVar("T", bound="RegionalIndexDef")


@_attrs_define
class RegionalIndexDef:
    """A transparent, decomposed regional index with fitted (proposed) weights

    Attributes:
        id (str):  Example: instability.
        name (str):
        components (list[RegionalIndexDefComponentsItem]):
        weights (RegionalIndexDefWeights): Component key → weight (sums to 1)
        target (str): Outlook family the index should anticipate Example: conflict:battles.
        weights_version (int):
        weights_origin (RegionalIndexDefWeightsOrigin):
        enabled (bool):
        skill (None | RegionalIndexDefSkillType0):
        description (None | str | Unset):
        floors (list[RegionalIndexDefFloorsItem] | Unset):
        auto_apply_fitted_weights (bool | Unset):  Default: False.
        built_in (bool | Unset):
        ready (bool | Unset): All component series exist
        updated_at (datetime.datetime | None | Unset):
    """

    id: str
    name: str
    components: list[RegionalIndexDefComponentsItem]
    weights: RegionalIndexDefWeights
    target: str
    weights_version: int
    weights_origin: RegionalIndexDefWeightsOrigin
    enabled: bool
    skill: None | RegionalIndexDefSkillType0
    description: None | str | Unset = UNSET
    floors: list[RegionalIndexDefFloorsItem] | Unset = UNSET
    auto_apply_fitted_weights: bool | Unset = False
    built_in: bool | Unset = UNSET
    ready: bool | Unset = UNSET
    updated_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.regional_index_def_skill_type_0 import RegionalIndexDefSkillType0

        id = self.id

        name = self.name

        components = []
        for components_item_data in self.components:
            components_item = components_item_data.to_dict()
            components.append(components_item)

        weights = self.weights.to_dict()

        target = self.target

        weights_version = self.weights_version

        weights_origin = self.weights_origin.value

        enabled = self.enabled

        skill: dict[str, Any] | None
        if isinstance(self.skill, RegionalIndexDefSkillType0):
            skill = self.skill.to_dict()
        else:
            skill = self.skill

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

        built_in = self.built_in

        ready = self.ready

        updated_at: None | str | Unset
        if isinstance(self.updated_at, Unset):
            updated_at = UNSET
        elif isinstance(self.updated_at, datetime.datetime):
            updated_at = self.updated_at.isoformat()
        else:
            updated_at = self.updated_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "components": components,
                "weights": weights,
                "target": target,
                "weightsVersion": weights_version,
                "weightsOrigin": weights_origin,
                "enabled": enabled,
                "skill": skill,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if floors is not UNSET:
            field_dict["floors"] = floors
        if auto_apply_fitted_weights is not UNSET:
            field_dict["autoApplyFittedWeights"] = auto_apply_fitted_weights
        if built_in is not UNSET:
            field_dict["builtIn"] = built_in
        if ready is not UNSET:
            field_dict["ready"] = ready
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.regional_index_def_components_item import (
            RegionalIndexDefComponentsItem,
        )
        from ..models.regional_index_def_floors_item import RegionalIndexDefFloorsItem
        from ..models.regional_index_def_skill_type_0 import RegionalIndexDefSkillType0
        from ..models.regional_index_def_weights import RegionalIndexDefWeights

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        components = []
        _components = d.pop("components")
        for components_item_data in _components:
            components_item = RegionalIndexDefComponentsItem.from_dict(
                components_item_data
            )

            components.append(components_item)

        weights = RegionalIndexDefWeights.from_dict(d.pop("weights"))

        target = d.pop("target")

        weights_version = d.pop("weightsVersion")

        weights_origin = RegionalIndexDefWeightsOrigin(d.pop("weightsOrigin"))

        enabled = d.pop("enabled")

        def _parse_skill(data: object) -> None | RegionalIndexDefSkillType0:
            if data is None:
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                skill_type_0 = RegionalIndexDefSkillType0.from_dict(data)

                return skill_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RegionalIndexDefSkillType0, data)

        skill = _parse_skill(d.pop("skill"))

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        _floors = d.pop("floors", UNSET)
        floors: list[RegionalIndexDefFloorsItem] | Unset = UNSET
        if _floors is not UNSET:
            floors = []
            for floors_item_data in _floors:
                floors_item = RegionalIndexDefFloorsItem.from_dict(floors_item_data)

                floors.append(floors_item)

        auto_apply_fitted_weights = d.pop("autoApplyFittedWeights", UNSET)

        built_in = d.pop("builtIn", UNSET)

        ready = d.pop("ready", UNSET)

        def _parse_updated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                updated_at_type_0 = datetime.datetime.fromisoformat(data)

                return updated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        updated_at = _parse_updated_at(d.pop("updatedAt", UNSET))

        regional_index_def = cls(
            id=id,
            name=name,
            components=components,
            weights=weights,
            target=target,
            weights_version=weights_version,
            weights_origin=weights_origin,
            enabled=enabled,
            skill=skill,
            description=description,
            floors=floors,
            auto_apply_fitted_weights=auto_apply_fitted_weights,
            built_in=built_in,
            ready=ready,
            updated_at=updated_at,
        )

        regional_index_def.additional_properties = d
        return regional_index_def

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
