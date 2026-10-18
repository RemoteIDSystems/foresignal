from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.exposure_area import ExposureArea
    from ..models.exposure_dependents import ExposureDependents
    from ..models.exposure_facilities import ExposureFacilities
    from ..models.exposure_places import ExposurePlaces
    from ..models.exposure_population import ExposurePopulation
    from ..models.exposure_traffic import ExposureTraffic
    from ..models.exposure_watchlist_entities import ExposureWatchlistEntities


T = TypeVar("T", bound="Exposure")


@_attrs_define
class Exposure:
    """What is inside an affected area: reference facilities, places, watchlist entities, live traffic, optional population
    and infrastructure dependents. Each section carries status and attribution.

        Attributes:
            area (ExposureArea): The evaluated area
            computed_at (datetime.datetime):
            facilities (ExposureFacilities): Current reference facilities by kind (094)
            places (ExposurePlaces): Places (036) inside the area
            watchlist_entities (ExposureWatchlistEntities): Current counts of watchlist entities inside (036/098)
            traffic (ExposureTraffic): Live aircraft and vessels inside
            population (ExposurePopulation): Optional GHSL population estimate (pack)
            dependents (ExposureDependents | Unset): Infrastructure dependents summary (104)
    """

    area: ExposureArea
    computed_at: datetime.datetime
    facilities: ExposureFacilities
    places: ExposurePlaces
    watchlist_entities: ExposureWatchlistEntities
    traffic: ExposureTraffic
    population: ExposurePopulation
    dependents: ExposureDependents | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        area = self.area.to_dict()

        computed_at = self.computed_at.isoformat()

        facilities = self.facilities.to_dict()

        places = self.places.to_dict()

        watchlist_entities = self.watchlist_entities.to_dict()

        traffic = self.traffic.to_dict()

        population = self.population.to_dict()

        dependents: dict[str, Any] | Unset = UNSET
        if not isinstance(self.dependents, Unset):
            dependents = self.dependents.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "area": area,
                "computedAt": computed_at,
                "facilities": facilities,
                "places": places,
                "watchlistEntities": watchlist_entities,
                "traffic": traffic,
                "population": population,
            }
        )
        if dependents is not UNSET:
            field_dict["dependents"] = dependents

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.exposure_area import ExposureArea
        from ..models.exposure_dependents import ExposureDependents
        from ..models.exposure_facilities import ExposureFacilities
        from ..models.exposure_places import ExposurePlaces
        from ..models.exposure_population import ExposurePopulation
        from ..models.exposure_traffic import ExposureTraffic
        from ..models.exposure_watchlist_entities import ExposureWatchlistEntities

        d = dict(src_dict)
        area = ExposureArea.from_dict(d.pop("area"))

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        facilities = ExposureFacilities.from_dict(d.pop("facilities"))

        places = ExposurePlaces.from_dict(d.pop("places"))

        watchlist_entities = ExposureWatchlistEntities.from_dict(
            d.pop("watchlistEntities")
        )

        traffic = ExposureTraffic.from_dict(d.pop("traffic"))

        population = ExposurePopulation.from_dict(d.pop("population"))

        _dependents = d.pop("dependents", UNSET)
        dependents: ExposureDependents | Unset
        if isinstance(_dependents, Unset):
            dependents = UNSET
        else:
            dependents = ExposureDependents.from_dict(_dependents)

        exposure = cls(
            area=area,
            computed_at=computed_at,
            facilities=facilities,
            places=places,
            watchlist_entities=watchlist_entities,
            traffic=traffic,
            population=population,
            dependents=dependents,
        )

        exposure.additional_properties = d
        return exposure

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
