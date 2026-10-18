from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.satellite_pass_payload_geometry_source import (
    SatellitePassPayloadGeometrySource,
)
from ..models.satellite_pass_payload_status import SatellitePassPayloadStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.satellite_pass_payload_products_item import (
        SatellitePassPayloadProductsItem,
    )
    from ..models.satellite_pass_payload_rf import SatellitePassPayloadRf


T = TypeVar("T", bound="SatellitePassPayload")


@_attrs_define
class SatellitePassPayload:
    """Payload of `satellite-pass` records (externalId `{station}:{satKey}:{aosEpochMinute}`); geometry: polar GeoTIFF
    footprint → approximate SGP4 swath → station point (054), named by `geometrySource`

        Attributes:
            status (SatellitePassPayloadStatus): `predicted` (064: Orekit from `celestrak-gp` elements), then `receiving`,
                `recorded`, `decoded` or `failed` (049); `missed` (064) when a predicted pass had no session by LOS + 20 min
                while the station was healthy (otherwise the predicted pass is tombstoned)
            satellite (str):
            sat_key (str):
            aos (datetime.datetime):
            title (str):
            norad (int | Unset):
            pipeline_id (str | Unset):
            frequency_hz (int | Unset):
            los (datetime.datetime | None | Unset):
            max_elevation_deg (float | Unset):
            rf (SatellitePassPayloadRf | Unset): RF session aggregate at LOS
            instruments (list[str] | Unset):
            products (list[SatellitePassPayloadProductsItem] | Unset):
            tle (None | str | Unset):
            satdump_version (None | str | Unset):
            attribution (Attribution | Unset): Structured attribution clients must render for a product or derived output
                (Weather-Imagery-Design §6.10)
            geometry_source (SatellitePassPayloadGeometrySource | Unset): Present when the record geometry is not the
                station point: `footprint` (the alpha-mask outline of the pass's polar GeoTIFF overlays, 054) or `swath` (an
                approximate SGP4 swath from the TLE, used when no GeoTIFF was usable; never tiled)
            tca (datetime.datetime | Unset): Predicted time of maximum elevation (064, predicted passes)
            aos_azimuth_deg (float | Unset): Predicted azimuth at AOS, degrees (064)
            los_azimuth_deg (float | Unset): Predicted azimuth at LOS, degrees (064)
            place (str | Unset): The place id (064 `satellite-passes`, externalId
                `place:{placeId}:{norad}:{aosEpochMinute}`)
            imminent (bool | Unset): 064 `satellite-passes`: true from 15 minutes before AOS until LOS (a new version near
                AOS, so `forecastHorizon` rules, which match `validFrom` = AOS on new versions only, and plain `imminent ==
                true` rules can fire)
    """

    status: SatellitePassPayloadStatus
    satellite: str
    sat_key: str
    aos: datetime.datetime
    title: str
    norad: int | Unset = UNSET
    pipeline_id: str | Unset = UNSET
    frequency_hz: int | Unset = UNSET
    los: datetime.datetime | None | Unset = UNSET
    max_elevation_deg: float | Unset = UNSET
    rf: SatellitePassPayloadRf | Unset = UNSET
    instruments: list[str] | Unset = UNSET
    products: list[SatellitePassPayloadProductsItem] | Unset = UNSET
    tle: None | str | Unset = UNSET
    satdump_version: None | str | Unset = UNSET
    attribution: Attribution | Unset = UNSET
    geometry_source: SatellitePassPayloadGeometrySource | Unset = UNSET
    tca: datetime.datetime | Unset = UNSET
    aos_azimuth_deg: float | Unset = UNSET
    los_azimuth_deg: float | Unset = UNSET
    place: str | Unset = UNSET
    imminent: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        satellite = self.satellite

        sat_key = self.sat_key

        aos = self.aos.isoformat()

        title = self.title

        norad = self.norad

        pipeline_id = self.pipeline_id

        frequency_hz = self.frequency_hz

        los: None | str | Unset
        if isinstance(self.los, Unset):
            los = UNSET
        elif isinstance(self.los, datetime.datetime):
            los = self.los.isoformat()
        else:
            los = self.los

        max_elevation_deg = self.max_elevation_deg

        rf: dict[str, Any] | Unset = UNSET
        if not isinstance(self.rf, Unset):
            rf = self.rf.to_dict()

        instruments: list[str] | Unset = UNSET
        if not isinstance(self.instruments, Unset):
            instruments = self.instruments

        products: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.products, Unset):
            products = []
            for products_item_data in self.products:
                products_item = products_item_data.to_dict()
                products.append(products_item)

        tle: None | str | Unset
        if isinstance(self.tle, Unset):
            tle = UNSET
        else:
            tle = self.tle

        satdump_version: None | str | Unset
        if isinstance(self.satdump_version, Unset):
            satdump_version = UNSET
        else:
            satdump_version = self.satdump_version

        attribution: dict[str, Any] | Unset = UNSET
        if not isinstance(self.attribution, Unset):
            attribution = self.attribution.to_dict()

        geometry_source: str | Unset = UNSET
        if not isinstance(self.geometry_source, Unset):
            geometry_source = self.geometry_source.value

        tca: str | Unset = UNSET
        if not isinstance(self.tca, Unset):
            tca = self.tca.isoformat()

        aos_azimuth_deg = self.aos_azimuth_deg

        los_azimuth_deg = self.los_azimuth_deg

        place = self.place

        imminent = self.imminent

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
                "satellite": satellite,
                "satKey": sat_key,
                "aos": aos,
                "title": title,
            }
        )
        if norad is not UNSET:
            field_dict["norad"] = norad
        if pipeline_id is not UNSET:
            field_dict["pipelineId"] = pipeline_id
        if frequency_hz is not UNSET:
            field_dict["frequencyHz"] = frequency_hz
        if los is not UNSET:
            field_dict["los"] = los
        if max_elevation_deg is not UNSET:
            field_dict["maxElevationDeg"] = max_elevation_deg
        if rf is not UNSET:
            field_dict["rf"] = rf
        if instruments is not UNSET:
            field_dict["instruments"] = instruments
        if products is not UNSET:
            field_dict["products"] = products
        if tle is not UNSET:
            field_dict["tle"] = tle
        if satdump_version is not UNSET:
            field_dict["satdumpVersion"] = satdump_version
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if geometry_source is not UNSET:
            field_dict["geometrySource"] = geometry_source
        if tca is not UNSET:
            field_dict["tca"] = tca
        if aos_azimuth_deg is not UNSET:
            field_dict["aosAzimuthDeg"] = aos_azimuth_deg
        if los_azimuth_deg is not UNSET:
            field_dict["losAzimuthDeg"] = los_azimuth_deg
        if place is not UNSET:
            field_dict["place"] = place
        if imminent is not UNSET:
            field_dict["imminent"] = imminent

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.satellite_pass_payload_products_item import (
            SatellitePassPayloadProductsItem,
        )
        from ..models.satellite_pass_payload_rf import SatellitePassPayloadRf

        d = dict(src_dict)
        status = SatellitePassPayloadStatus(d.pop("status"))

        satellite = d.pop("satellite")

        sat_key = d.pop("satKey")

        aos = datetime.datetime.fromisoformat(d.pop("aos"))

        title = d.pop("title")

        norad = d.pop("norad", UNSET)

        pipeline_id = d.pop("pipelineId", UNSET)

        frequency_hz = d.pop("frequencyHz", UNSET)

        def _parse_los(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                los_type_0 = datetime.datetime.fromisoformat(data)

                return los_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        los = _parse_los(d.pop("los", UNSET))

        max_elevation_deg = d.pop("maxElevationDeg", UNSET)

        _rf = d.pop("rf", UNSET)
        rf: SatellitePassPayloadRf | Unset
        if isinstance(_rf, Unset):
            rf = UNSET
        else:
            rf = SatellitePassPayloadRf.from_dict(_rf)

        instruments = cast(list[str], d.pop("instruments", UNSET))

        _products = d.pop("products", UNSET)
        products: list[SatellitePassPayloadProductsItem] | Unset = UNSET
        if _products is not UNSET:
            products = []
            for products_item_data in _products:
                products_item = SatellitePassPayloadProductsItem.from_dict(
                    products_item_data
                )

                products.append(products_item)

        def _parse_tle(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        tle = _parse_tle(d.pop("tle", UNSET))

        def _parse_satdump_version(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        satdump_version = _parse_satdump_version(d.pop("satdumpVersion", UNSET))

        _attribution = d.pop("attribution", UNSET)
        attribution: Attribution | Unset
        if isinstance(_attribution, Unset):
            attribution = UNSET
        else:
            attribution = Attribution.from_dict(_attribution)

        _geometry_source = d.pop("geometrySource", UNSET)
        geometry_source: SatellitePassPayloadGeometrySource | Unset
        if isinstance(_geometry_source, Unset):
            geometry_source = UNSET
        else:
            geometry_source = SatellitePassPayloadGeometrySource(_geometry_source)

        _tca = d.pop("tca", UNSET)
        tca: datetime.datetime | Unset
        if isinstance(_tca, Unset):
            tca = UNSET
        else:
            tca = datetime.datetime.fromisoformat(_tca)

        aos_azimuth_deg = d.pop("aosAzimuthDeg", UNSET)

        los_azimuth_deg = d.pop("losAzimuthDeg", UNSET)

        place = d.pop("place", UNSET)

        imminent = d.pop("imminent", UNSET)

        satellite_pass_payload = cls(
            status=status,
            satellite=satellite,
            sat_key=sat_key,
            aos=aos,
            title=title,
            norad=norad,
            pipeline_id=pipeline_id,
            frequency_hz=frequency_hz,
            los=los,
            max_elevation_deg=max_elevation_deg,
            rf=rf,
            instruments=instruments,
            products=products,
            tle=tle,
            satdump_version=satdump_version,
            attribution=attribution,
            geometry_source=geometry_source,
            tca=tca,
            aos_azimuth_deg=aos_azimuth_deg,
            los_azimuth_deg=los_azimuth_deg,
            place=place,
            imminent=imminent,
        )

        satellite_pass_payload.additional_properties = d
        return satellite_pass_payload

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
