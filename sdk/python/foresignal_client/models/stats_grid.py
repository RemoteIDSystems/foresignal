from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.stats_grid_backend import StatsGridBackend
from ..models.stats_grid_cell_system import StatsGridCellSystem
from ..models.stats_grid_type import StatsGridType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.stats_grid_features_item import StatsGridFeaturesItem


T = TypeVar("T", bound="StatsGrid")


@_attrs_define
class StatsGrid:
    """Spatial aggregate as a GeoJSON FeatureCollection of cells (038)

    Attributes:
        type_ (StatsGridType):
        cell_system (StatsGridCellSystem):
        features (list[StatsGridFeaturesItem]):
        res (int | None | Unset): H3 resolution (rollup mode)
        size_km (float | None | Unset):
        measure (str | Unset):
        backend (StatsGridBackend | Unset):
    """

    type_: StatsGridType
    cell_system: StatsGridCellSystem
    features: list[StatsGridFeaturesItem]
    res: int | None | Unset = UNSET
    size_km: float | None | Unset = UNSET
    measure: str | Unset = UNSET
    backend: StatsGridBackend | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        cell_system = self.cell_system.value

        features = []
        for features_item_data in self.features:
            features_item = features_item_data.to_dict()
            features.append(features_item)

        res: int | None | Unset
        if isinstance(self.res, Unset):
            res = UNSET
        else:
            res = self.res

        size_km: float | None | Unset
        if isinstance(self.size_km, Unset):
            size_km = UNSET
        else:
            size_km = self.size_km

        measure = self.measure

        backend: str | Unset = UNSET
        if not isinstance(self.backend, Unset):
            backend = self.backend.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "cellSystem": cell_system,
                "features": features,
            }
        )
        if res is not UNSET:
            field_dict["res"] = res
        if size_km is not UNSET:
            field_dict["sizeKm"] = size_km
        if measure is not UNSET:
            field_dict["measure"] = measure
        if backend is not UNSET:
            field_dict["backend"] = backend

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.stats_grid_features_item import StatsGridFeaturesItem

        d = dict(src_dict)
        type_ = StatsGridType(d.pop("type"))

        cell_system = StatsGridCellSystem(d.pop("cellSystem"))

        features = []
        _features = d.pop("features")
        for features_item_data in _features:
            features_item = StatsGridFeaturesItem.from_dict(features_item_data)

            features.append(features_item)

        def _parse_res(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        res = _parse_res(d.pop("res", UNSET))

        def _parse_size_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        size_km = _parse_size_km(d.pop("sizeKm", UNSET))

        measure = d.pop("measure", UNSET)

        _backend = d.pop("backend", UNSET)
        backend: StatsGridBackend | Unset
        if isinstance(_backend, Unset):
            backend = UNSET
        else:
            backend = StatsGridBackend(_backend)

        stats_grid = cls(
            type_=type_,
            cell_system=cell_system,
            features=features,
            res=res,
            size_km=size_km,
            measure=measure,
            backend=backend,
        )

        stats_grid.additional_properties = d
        return stats_grid

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
