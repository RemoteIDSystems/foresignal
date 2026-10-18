from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ReferenceImportRequestParams")


@_attrs_define
class ReferenceImportRequestParams:
    """Dataset parameters

    Attributes:
        file (str | Unset): ucdp-ged: CSV (or the downloaded ZIP holding it) path relative to
            `${FORESIGNAL_DATA_DIR}/imports`
        use_api (bool | Unset): ucdp-ged: use the UCDP API instead of a file
        year_from (int | Unset):
        year_to (int | Unset):
        countries (list[str] | Unset): acled: ISO country codes
        min_magnitude (float | Unset): usgs-comcat: minimum magnitude Default: 4.5.
        hazard_types (list[str] | Unset): gdacs: hazard types (default all)
        version (str | Unset): ucdp-ged with useApi: the GED release to read, e.g. `26.1` (required with useApi)
    """

    file: str | Unset = UNSET
    use_api: bool | Unset = UNSET
    year_from: int | Unset = UNSET
    year_to: int | Unset = UNSET
    countries: list[str] | Unset = UNSET
    min_magnitude: float | Unset = 4.5
    hazard_types: list[str] | Unset = UNSET
    version: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        file = self.file

        use_api = self.use_api

        year_from = self.year_from

        year_to = self.year_to

        countries: list[str] | Unset = UNSET
        if not isinstance(self.countries, Unset):
            countries = self.countries

        min_magnitude = self.min_magnitude

        hazard_types: list[str] | Unset = UNSET
        if not isinstance(self.hazard_types, Unset):
            hazard_types = self.hazard_types

        version = self.version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if file is not UNSET:
            field_dict["file"] = file
        if use_api is not UNSET:
            field_dict["useApi"] = use_api
        if year_from is not UNSET:
            field_dict["yearFrom"] = year_from
        if year_to is not UNSET:
            field_dict["yearTo"] = year_to
        if countries is not UNSET:
            field_dict["countries"] = countries
        if min_magnitude is not UNSET:
            field_dict["minMagnitude"] = min_magnitude
        if hazard_types is not UNSET:
            field_dict["hazardTypes"] = hazard_types
        if version is not UNSET:
            field_dict["version"] = version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        file = d.pop("file", UNSET)

        use_api = d.pop("useApi", UNSET)

        year_from = d.pop("yearFrom", UNSET)

        year_to = d.pop("yearTo", UNSET)

        countries = cast(list[str], d.pop("countries", UNSET))

        min_magnitude = d.pop("minMagnitude", UNSET)

        hazard_types = cast(list[str], d.pop("hazardTypes", UNSET))

        version = d.pop("version", UNSET)

        reference_import_request_params = cls(
            file=file,
            use_api=use_api,
            year_from=year_from,
            year_to=year_to,
            countries=countries,
            min_magnitude=min_magnitude,
            hazard_types=hazard_types,
            version=version,
        )

        reference_import_request_params.additional_properties = d
        return reference_import_request_params

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
