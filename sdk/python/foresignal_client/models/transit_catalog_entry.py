from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="TransitCatalogEntry")


@_attrs_define
class TransitCatalogEntry:
    """One GTFS-RT feed row of the vendored MobilityData snapshot and whether it is enabled (070)

    Attributes:
        id (str): Mobility Database id Example: mdb-1234.
        provider (str):
        entity_type (str): The GTFS-RT entity types the URL serves, `|`-joined as in the catalog (`vp` vehicle
            positions, `tu` trip updates, `sa` service alerts; for example `tu|vp`); empty when the catalog declares none
            Example: vp.
        direct_download_url (str):
        enabled (bool): Listed in `foresignal.transit.catalog-feeds` and registered
        country_code (None | str | Unset):
        license_url (None | str | Unset):
        static_reference (None | str | Unset):
        source_id (None | str | Unset): The first registered source id (`transit-mdb-{id}` or `transit-alerts-mdb-{id}`)
            when enabled; trip updates are not ingested. `{id}` is the catalog id lower-cased with other characters replaced
            by hyphens (shortened with a hash suffix past 64 characters)
        source_ids (list[str] | Unset): Every source id registered for the row (a `sa|tu|vp` row registers both
            `transit-mdb-{id}` and `transit-alerts-mdb-{id}`); empty when not enabled
    """

    id: str
    provider: str
    entity_type: str
    direct_download_url: str
    enabled: bool
    country_code: None | str | Unset = UNSET
    license_url: None | str | Unset = UNSET
    static_reference: None | str | Unset = UNSET
    source_id: None | str | Unset = UNSET
    source_ids: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        provider = self.provider

        entity_type = self.entity_type

        direct_download_url = self.direct_download_url

        enabled = self.enabled

        country_code: None | str | Unset
        if isinstance(self.country_code, Unset):
            country_code = UNSET
        else:
            country_code = self.country_code

        license_url: None | str | Unset
        if isinstance(self.license_url, Unset):
            license_url = UNSET
        else:
            license_url = self.license_url

        static_reference: None | str | Unset
        if isinstance(self.static_reference, Unset):
            static_reference = UNSET
        else:
            static_reference = self.static_reference

        source_id: None | str | Unset
        if isinstance(self.source_id, Unset):
            source_id = UNSET
        else:
            source_id = self.source_id

        source_ids: list[str] | Unset = UNSET
        if not isinstance(self.source_ids, Unset):
            source_ids = self.source_ids

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "provider": provider,
                "entityType": entity_type,
                "directDownloadUrl": direct_download_url,
                "enabled": enabled,
            }
        )
        if country_code is not UNSET:
            field_dict["countryCode"] = country_code
        if license_url is not UNSET:
            field_dict["licenseUrl"] = license_url
        if static_reference is not UNSET:
            field_dict["staticReference"] = static_reference
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if source_ids is not UNSET:
            field_dict["sourceIds"] = source_ids

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        provider = d.pop("provider")

        entity_type = d.pop("entityType")

        direct_download_url = d.pop("directDownloadUrl")

        enabled = d.pop("enabled")

        def _parse_country_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        country_code = _parse_country_code(d.pop("countryCode", UNSET))

        def _parse_license_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        license_url = _parse_license_url(d.pop("licenseUrl", UNSET))

        def _parse_static_reference(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        static_reference = _parse_static_reference(d.pop("staticReference", UNSET))

        def _parse_source_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source_id = _parse_source_id(d.pop("sourceId", UNSET))

        source_ids = cast(list[str], d.pop("sourceIds", UNSET))

        transit_catalog_entry = cls(
            id=id,
            provider=provider,
            entity_type=entity_type,
            direct_download_url=direct_download_url,
            enabled=enabled,
            country_code=country_code,
            license_url=license_url,
            static_reference=static_reference,
            source_id=source_id,
            source_ids=source_ids,
        )

        transit_catalog_entry.additional_properties = d
        return transit_catalog_entry

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
