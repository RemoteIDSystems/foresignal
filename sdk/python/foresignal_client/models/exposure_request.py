from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define

from ..models.exposure_request_population import ExposureRequestPopulation
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="ExposureRequest")


@_attrs_define
class ExposureRequest:
    """Exposure query: exactly one of geometry, placeId or eventId

    Attributes:
        geometry (Geometry | Unset): GeoJSON geometry (RFC 7946), WGS84
        place_id (str | Unset):
        event_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        population (ExposureRequestPopulation | Unset): auto: section reports not-installed without a pack; required:
            409 PACK_NOT_INSTALLED Default: ExposureRequestPopulation.AUTO.
        dependents (bool | Unset): Include the infrastructure dependents summary (104) Default: True.
    """

    geometry: Geometry | Unset = UNSET
    place_id: str | Unset = UNSET
    event_id: str | Unset = UNSET
    population: ExposureRequestPopulation | Unset = ExposureRequestPopulation.AUTO
    dependents: bool | Unset = True

    def to_dict(self) -> dict[str, Any]:
        geometry: dict[str, Any] | Unset = UNSET
        if not isinstance(self.geometry, Unset):
            geometry = self.geometry.to_dict()

        place_id = self.place_id

        event_id = self.event_id

        population: str | Unset = UNSET
        if not isinstance(self.population, Unset):
            population = self.population.value

        dependents = self.dependents

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if event_id is not UNSET:
            field_dict["eventId"] = event_id
        if population is not UNSET:
            field_dict["population"] = population
        if dependents is not UNSET:
            field_dict["dependents"] = dependents

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        _geometry = d.pop("geometry", UNSET)
        geometry: Geometry | Unset
        if isinstance(_geometry, Unset):
            geometry = UNSET
        else:
            geometry = Geometry.from_dict(_geometry)

        place_id = d.pop("placeId", UNSET)

        event_id = d.pop("eventId", UNSET)

        _population = d.pop("population", UNSET)
        population: ExposureRequestPopulation | Unset
        if isinstance(_population, Unset):
            population = UNSET
        else:
            population = ExposureRequestPopulation(_population)

        dependents = d.pop("dependents", UNSET)

        exposure_request = cls(
            geometry=geometry,
            place_id=place_id,
            event_id=event_id,
            population=population,
            dependents=dependents,
        )

        return exposure_request
