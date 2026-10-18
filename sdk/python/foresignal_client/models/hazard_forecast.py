from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.hazard_forecast_hazard_type import HazardForecastHazardType
from ..models.hazard_forecast_probability_model import HazardForecastProbabilityModel
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.hazard_forecast_details import HazardForecastDetails
    from ..models.hazard_forecast_probability_field_item import (
        HazardForecastProbabilityFieldItem,
    )
    from ..models.hazard_forecast_probability_range_type_0 import (
        HazardForecastProbabilityRangeType0,
    )


T = TypeVar("T", bound="HazardForecast")


@_attrs_define
class HazardForecast:
    """A hazard forecast adapted from an ingested record, with its published probability model

    Attributes:
        hazard_type (HazardForecastHazardType):
        source_ref (str): Pinned record reference `record:{sourceId}/{externalId}@{seq}`
        issued_at (datetime.datetime):
        valid_from (datetime.datetime):
        valid_to (datetime.datetime):
        probability_model (HazardForecastProbabilityModel): `none` for cones and categorical products (context only,
            never a probability)
        issuer (str | Unset):  Default: 'official'.
        geometry (Geometry | None | Unset):
        probability_field (list[HazardForecastProbabilityFieldItem] | Unset): Published probability bands, highest
            first; geometries only on getProjectedImpact (lists leave them out)
        category (None | str | Unset): Producer category (e.g. ERO `moderate`, flood category)
        probability_range (HazardForecastProbabilityRangeType0 | None | Unset):
        note (None | str | Unset):  Example: The cone covers about two thirds of historical track errors.
        hazard_key (str | Unset): The hazard's identity across versions (debounce, supersession, question dedup), e.g.
            nhc-wind-probs:34, spc-outlooks:day1:torn, wpc-ero-d1, nwps-gauges:{lid}
        source_refs (list[str] | Unset): Every pinned record behind the forecast (sourceRef is the newest)
        point_radius_km (float | None | Unset): Radius a published point probability applies within (SPC and WPC: 25
            miles = 40.2336 km)
        details (HazardForecastDetails | Unset): Source-specific facts: NHC thresholdKt and horizonHours; SPC day and
            layer; WPC day and categoryName; a gauge's basis (forecast or persistence), categories, calibration {basis,
            minCategory, n, hits, p, lower, upper, minSamples} and reason when no probability is derived (below-category,
            uncalibrated, no-geometry); a storm cell's reason (no-position-error)
    """

    hazard_type: HazardForecastHazardType
    source_ref: str
    issued_at: datetime.datetime
    valid_from: datetime.datetime
    valid_to: datetime.datetime
    probability_model: HazardForecastProbabilityModel
    issuer: str | Unset = "official"
    geometry: Geometry | None | Unset = UNSET
    probability_field: list[HazardForecastProbabilityFieldItem] | Unset = UNSET
    category: None | str | Unset = UNSET
    probability_range: HazardForecastProbabilityRangeType0 | None | Unset = UNSET
    note: None | str | Unset = UNSET
    hazard_key: str | Unset = UNSET
    source_refs: list[str] | Unset = UNSET
    point_radius_km: float | None | Unset = UNSET
    details: HazardForecastDetails | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry
        from ..models.hazard_forecast_probability_range_type_0 import (
            HazardForecastProbabilityRangeType0,
        )

        hazard_type = self.hazard_type.value

        source_ref = self.source_ref

        issued_at = self.issued_at.isoformat()

        valid_from = self.valid_from.isoformat()

        valid_to = self.valid_to.isoformat()

        probability_model = self.probability_model.value

        issuer = self.issuer

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        probability_field: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.probability_field, Unset):
            probability_field = []
            for probability_field_item_data in self.probability_field:
                probability_field_item = probability_field_item_data.to_dict()
                probability_field.append(probability_field_item)

        category: None | str | Unset
        if isinstance(self.category, Unset):
            category = UNSET
        else:
            category = self.category

        probability_range: dict[str, Any] | None | Unset
        if isinstance(self.probability_range, Unset):
            probability_range = UNSET
        elif isinstance(self.probability_range, HazardForecastProbabilityRangeType0):
            probability_range = self.probability_range.to_dict()
        else:
            probability_range = self.probability_range

        note: None | str | Unset
        if isinstance(self.note, Unset):
            note = UNSET
        else:
            note = self.note

        hazard_key = self.hazard_key

        source_refs: list[str] | Unset = UNSET
        if not isinstance(self.source_refs, Unset):
            source_refs = self.source_refs

        point_radius_km: float | None | Unset
        if isinstance(self.point_radius_km, Unset):
            point_radius_km = UNSET
        else:
            point_radius_km = self.point_radius_km

        details: dict[str, Any] | Unset = UNSET
        if not isinstance(self.details, Unset):
            details = self.details.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "hazardType": hazard_type,
                "sourceRef": source_ref,
                "issuedAt": issued_at,
                "validFrom": valid_from,
                "validTo": valid_to,
                "probabilityModel": probability_model,
            }
        )
        if issuer is not UNSET:
            field_dict["issuer"] = issuer
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if probability_field is not UNSET:
            field_dict["probabilityField"] = probability_field
        if category is not UNSET:
            field_dict["category"] = category
        if probability_range is not UNSET:
            field_dict["probabilityRange"] = probability_range
        if note is not UNSET:
            field_dict["note"] = note
        if hazard_key is not UNSET:
            field_dict["hazardKey"] = hazard_key
        if source_refs is not UNSET:
            field_dict["sourceRefs"] = source_refs
        if point_radius_km is not UNSET:
            field_dict["pointRadiusKm"] = point_radius_km
        if details is not UNSET:
            field_dict["details"] = details

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.hazard_forecast_details import HazardForecastDetails
        from ..models.hazard_forecast_probability_field_item import (
            HazardForecastProbabilityFieldItem,
        )
        from ..models.hazard_forecast_probability_range_type_0 import (
            HazardForecastProbabilityRangeType0,
        )

        d = dict(src_dict)
        hazard_type = HazardForecastHazardType(d.pop("hazardType"))

        source_ref = d.pop("sourceRef")

        issued_at = datetime.datetime.fromisoformat(d.pop("issuedAt"))

        valid_from = datetime.datetime.fromisoformat(d.pop("validFrom"))

        valid_to = datetime.datetime.fromisoformat(d.pop("validTo"))

        probability_model = HazardForecastProbabilityModel(d.pop("probabilityModel"))

        issuer = d.pop("issuer", UNSET)

        def _parse_geometry(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        _probability_field = d.pop("probabilityField", UNSET)
        probability_field: list[HazardForecastProbabilityFieldItem] | Unset = UNSET
        if _probability_field is not UNSET:
            probability_field = []
            for probability_field_item_data in _probability_field:
                probability_field_item = HazardForecastProbabilityFieldItem.from_dict(
                    probability_field_item_data
                )

                probability_field.append(probability_field_item)

        def _parse_category(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        category = _parse_category(d.pop("category", UNSET))

        def _parse_probability_range(
            data: object,
        ) -> HazardForecastProbabilityRangeType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                probability_range_type_0 = (
                    HazardForecastProbabilityRangeType0.from_dict(data)
                )

                return probability_range_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(HazardForecastProbabilityRangeType0 | None | Unset, data)

        probability_range = _parse_probability_range(d.pop("probabilityRange", UNSET))

        def _parse_note(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        note = _parse_note(d.pop("note", UNSET))

        hazard_key = d.pop("hazardKey", UNSET)

        source_refs = cast(list[str], d.pop("sourceRefs", UNSET))

        def _parse_point_radius_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        point_radius_km = _parse_point_radius_km(d.pop("pointRadiusKm", UNSET))

        _details = d.pop("details", UNSET)
        details: HazardForecastDetails | Unset
        if isinstance(_details, Unset):
            details = UNSET
        else:
            details = HazardForecastDetails.from_dict(_details)

        hazard_forecast = cls(
            hazard_type=hazard_type,
            source_ref=source_ref,
            issued_at=issued_at,
            valid_from=valid_from,
            valid_to=valid_to,
            probability_model=probability_model,
            issuer=issuer,
            geometry=geometry,
            probability_field=probability_field,
            category=category,
            probability_range=probability_range,
            note=note,
            hazard_key=hazard_key,
            source_refs=source_refs,
            point_radius_km=point_radius_km,
            details=details,
        )

        hazard_forecast.additional_properties = d
        return hazard_forecast

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
