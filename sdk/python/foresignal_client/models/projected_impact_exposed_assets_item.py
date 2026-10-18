from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ProjectedImpactExposedAssetsItem")


@_attrs_define
class ProjectedImpactExposedAssetsItem:
    """Exposed facility, place or area

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        p_hazard (float):
        title (str | Unset):
        kind (str | Unset):
        distance_km (float | None | Unset):
        population (int | None | Unset):
        band (None | str | Unset): The published band at the asset
    """

    ref: str
    p_hazard: float
    title: str | Unset = UNSET
    kind: str | Unset = UNSET
    distance_km: float | None | Unset = UNSET
    population: int | None | Unset = UNSET
    band: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        p_hazard = self.p_hazard

        title = self.title

        kind = self.kind

        distance_km: float | None | Unset
        if isinstance(self.distance_km, Unset):
            distance_km = UNSET
        else:
            distance_km = self.distance_km

        population: int | None | Unset
        if isinstance(self.population, Unset):
            population = UNSET
        else:
            population = self.population

        band: None | str | Unset
        if isinstance(self.band, Unset):
            band = UNSET
        else:
            band = self.band

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "pHazard": p_hazard,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if kind is not UNSET:
            field_dict["kind"] = kind
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km
        if population is not UNSET:
            field_dict["population"] = population
        if band is not UNSET:
            field_dict["band"] = band

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ref = d.pop("ref")

        p_hazard = d.pop("pHazard")

        title = d.pop("title", UNSET)

        kind = d.pop("kind", UNSET)

        def _parse_distance_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        distance_km = _parse_distance_km(d.pop("distanceKm", UNSET))

        def _parse_population(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        population = _parse_population(d.pop("population", UNSET))

        def _parse_band(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        band = _parse_band(d.pop("band", UNSET))

        projected_impact_exposed_assets_item = cls(
            ref=ref,
            p_hazard=p_hazard,
            title=title,
            kind=kind,
            distance_km=distance_km,
            population=population,
            band=band,
        )

        projected_impact_exposed_assets_item.additional_properties = d
        return projected_impact_exposed_assets_item

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
