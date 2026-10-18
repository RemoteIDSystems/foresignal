from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ingest_device_setup_gpslogger import IngestDeviceSetupGpslogger
    from ..models.ingest_device_setup_overland import IngestDeviceSetupOverland
    from ..models.ingest_device_setup_owntracks import IngestDeviceSetupOwntracks
    from ..models.ingest_device_setup_traccar import IngestDeviceSetupTraccar


T = TypeVar("T", bound="IngestDeviceSetup")


@_attrs_define
class IngestDeviceSetup:
    """Ready-to-use tracker app settings, based on publicBaseUrl (039)

    Attributes:
        owntracks (IngestDeviceSetupOwntracks | Unset): OwnTracks .otrc JSON (mode 3 HTTP, url, auth, username,
            deviceId)
        overland (IngestDeviceSetupOverland | Unset): Overland settings
        traccar (IngestDeviceSetupTraccar | Unset): Traccar Client / OsmAnd settings
        gpslogger (IngestDeviceSetupGpslogger | Unset): GPSLogger custom URL
    """

    owntracks: IngestDeviceSetupOwntracks | Unset = UNSET
    overland: IngestDeviceSetupOverland | Unset = UNSET
    traccar: IngestDeviceSetupTraccar | Unset = UNSET
    gpslogger: IngestDeviceSetupGpslogger | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        owntracks: dict[str, Any] | Unset = UNSET
        if not isinstance(self.owntracks, Unset):
            owntracks = self.owntracks.to_dict()

        overland: dict[str, Any] | Unset = UNSET
        if not isinstance(self.overland, Unset):
            overland = self.overland.to_dict()

        traccar: dict[str, Any] | Unset = UNSET
        if not isinstance(self.traccar, Unset):
            traccar = self.traccar.to_dict()

        gpslogger: dict[str, Any] | Unset = UNSET
        if not isinstance(self.gpslogger, Unset):
            gpslogger = self.gpslogger.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if owntracks is not UNSET:
            field_dict["owntracks"] = owntracks
        if overland is not UNSET:
            field_dict["overland"] = overland
        if traccar is not UNSET:
            field_dict["traccar"] = traccar
        if gpslogger is not UNSET:
            field_dict["gpslogger"] = gpslogger

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ingest_device_setup_gpslogger import IngestDeviceSetupGpslogger
        from ..models.ingest_device_setup_overland import IngestDeviceSetupOverland
        from ..models.ingest_device_setup_owntracks import IngestDeviceSetupOwntracks
        from ..models.ingest_device_setup_traccar import IngestDeviceSetupTraccar

        d = dict(src_dict)
        _owntracks = d.pop("owntracks", UNSET)
        owntracks: IngestDeviceSetupOwntracks | Unset
        if isinstance(_owntracks, Unset):
            owntracks = UNSET
        else:
            owntracks = IngestDeviceSetupOwntracks.from_dict(_owntracks)

        _overland = d.pop("overland", UNSET)
        overland: IngestDeviceSetupOverland | Unset
        if isinstance(_overland, Unset):
            overland = UNSET
        else:
            overland = IngestDeviceSetupOverland.from_dict(_overland)

        _traccar = d.pop("traccar", UNSET)
        traccar: IngestDeviceSetupTraccar | Unset
        if isinstance(_traccar, Unset):
            traccar = UNSET
        else:
            traccar = IngestDeviceSetupTraccar.from_dict(_traccar)

        _gpslogger = d.pop("gpslogger", UNSET)
        gpslogger: IngestDeviceSetupGpslogger | Unset
        if isinstance(_gpslogger, Unset):
            gpslogger = UNSET
        else:
            gpslogger = IngestDeviceSetupGpslogger.from_dict(_gpslogger)

        ingest_device_setup = cls(
            owntracks=owntracks,
            overland=overland,
            traccar=traccar,
            gpslogger=gpslogger,
        )

        ingest_device_setup.additional_properties = d
        return ingest_device_setup

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
