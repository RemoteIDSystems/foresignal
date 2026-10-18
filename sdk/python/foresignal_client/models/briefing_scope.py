from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="BriefingScope")


@_attrs_define
class BriefingScope:
    """
    Attributes:
        sources (list[str] | Unset):
        kinds (list[str] | Unset):
        bbox (None | str | Unset):
        rule_ids (list[UUID] | Unset):
        include_health (bool | Unset):  Default: True.
        place_ids (list[str] | Unset): Places (036) whose areas bound the scope; digest headings name the place
        view_id (None | str | Unset): Saved view id or slug (036)
        country (list[str] | Unset): ISO2 filter (040)
        admin1 (list[str] | Unset): ISO 3166-2 filter (040)
        county (list[str] | Unset): FIPS5 filter (040)
        places (list[str] | Unset): Place ids for the "Weather at places" section (empty = none); unknown ids → 400
        weather_products (list[str] | Unset): Products for the section (default: those backing place-weather: mrms-
            reflectivity, mrms-mesh-60m, mrms-precip-rate, hrrr-smoke, gfs-gust, gfs-apcp); they choose the radar and MESH
            values and the GFS and HRRR rows (lightning, NWS alerts and the NWS forecast are always shown); unknown ids →
            400
        include_alert_quality (bool | Unset): Include the weekly alert-quality section: lowest precision (at least 10
            labeled), noisiest rules by alerts per day, rules with open tuning suggestions (115) Default: False.
    """

    sources: list[str] | Unset = UNSET
    kinds: list[str] | Unset = UNSET
    bbox: None | str | Unset = UNSET
    rule_ids: list[UUID] | Unset = UNSET
    include_health: bool | Unset = True
    place_ids: list[str] | Unset = UNSET
    view_id: None | str | Unset = UNSET
    country: list[str] | Unset = UNSET
    admin1: list[str] | Unset = UNSET
    county: list[str] | Unset = UNSET
    places: list[str] | Unset = UNSET
    weather_products: list[str] | Unset = UNSET
    include_alert_quality: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        sources: list[str] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources

        kinds: list[str] | Unset = UNSET
        if not isinstance(self.kinds, Unset):
            kinds = self.kinds

        bbox: None | str | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        else:
            bbox = self.bbox

        rule_ids: list[str] | Unset = UNSET
        if not isinstance(self.rule_ids, Unset):
            rule_ids = []
            for rule_ids_item_data in self.rule_ids:
                rule_ids_item = str(rule_ids_item_data)
                rule_ids.append(rule_ids_item)

        include_health = self.include_health

        place_ids: list[str] | Unset = UNSET
        if not isinstance(self.place_ids, Unset):
            place_ids = self.place_ids

        view_id: None | str | Unset
        if isinstance(self.view_id, Unset):
            view_id = UNSET
        else:
            view_id = self.view_id

        country: list[str] | Unset = UNSET
        if not isinstance(self.country, Unset):
            country = self.country

        admin1: list[str] | Unset = UNSET
        if not isinstance(self.admin1, Unset):
            admin1 = self.admin1

        county: list[str] | Unset = UNSET
        if not isinstance(self.county, Unset):
            county = self.county

        places: list[str] | Unset = UNSET
        if not isinstance(self.places, Unset):
            places = self.places

        weather_products: list[str] | Unset = UNSET
        if not isinstance(self.weather_products, Unset):
            weather_products = self.weather_products

        include_alert_quality = self.include_alert_quality

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if sources is not UNSET:
            field_dict["sources"] = sources
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if rule_ids is not UNSET:
            field_dict["ruleIds"] = rule_ids
        if include_health is not UNSET:
            field_dict["includeHealth"] = include_health
        if place_ids is not UNSET:
            field_dict["placeIds"] = place_ids
        if view_id is not UNSET:
            field_dict["viewId"] = view_id
        if country is not UNSET:
            field_dict["country"] = country
        if admin1 is not UNSET:
            field_dict["admin1"] = admin1
        if county is not UNSET:
            field_dict["county"] = county
        if places is not UNSET:
            field_dict["places"] = places
        if weather_products is not UNSET:
            field_dict["weatherProducts"] = weather_products
        if include_alert_quality is not UNSET:
            field_dict["includeAlertQuality"] = include_alert_quality

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        sources = cast(list[str], d.pop("sources", UNSET))

        kinds = cast(list[str], d.pop("kinds", UNSET))

        def _parse_bbox(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        bbox = _parse_bbox(d.pop("bbox", UNSET))

        _rule_ids = d.pop("ruleIds", UNSET)
        rule_ids: list[UUID] | Unset = UNSET
        if _rule_ids is not UNSET:
            rule_ids = []
            for rule_ids_item_data in _rule_ids:
                rule_ids_item = UUID(rule_ids_item_data)

                rule_ids.append(rule_ids_item)

        include_health = d.pop("includeHealth", UNSET)

        place_ids = cast(list[str], d.pop("placeIds", UNSET))

        def _parse_view_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        view_id = _parse_view_id(d.pop("viewId", UNSET))

        country = cast(list[str], d.pop("country", UNSET))

        admin1 = cast(list[str], d.pop("admin1", UNSET))

        county = cast(list[str], d.pop("county", UNSET))

        places = cast(list[str], d.pop("places", UNSET))

        weather_products = cast(list[str], d.pop("weatherProducts", UNSET))

        include_alert_quality = d.pop("includeAlertQuality", UNSET)

        briefing_scope = cls(
            sources=sources,
            kinds=kinds,
            bbox=bbox,
            rule_ids=rule_ids,
            include_health=include_health,
            place_ids=place_ids,
            view_id=view_id,
            country=country,
            admin1=admin1,
            county=county,
            places=places,
            weather_products=weather_products,
            include_alert_quality=include_alert_quality,
        )

        briefing_scope.additional_properties = d
        return briefing_scope

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
