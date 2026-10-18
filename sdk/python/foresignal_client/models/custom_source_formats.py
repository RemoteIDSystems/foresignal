from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.custom_source_formats_additional_property import (
        CustomSourceFormatsAdditionalProperty,
    )
    from ..models.custom_source_formats_alertmanager import (
        CustomSourceFormatsAlertmanager,
    )
    from ..models.custom_source_formats_cap import CustomSourceFormatsCap
    from ..models.custom_source_formats_cloudevents import (
        CustomSourceFormatsCloudevents,
    )
    from ..models.custom_source_formats_gpslogger import CustomSourceFormatsGpslogger
    from ..models.custom_source_formats_native import CustomSourceFormatsNative
    from ..models.custom_source_formats_osmand import CustomSourceFormatsOsmand
    from ..models.custom_source_formats_overland import CustomSourceFormatsOverland
    from ..models.custom_source_formats_owntracks import CustomSourceFormatsOwntracks


T = TypeVar("T", bound="CustomSourceFormats")


@_attrs_define
class CustomSourceFormats:
    """Per-format options of a custom source (024); keys are format ids

    Attributes:
        native (CustomSourceFormatsNative | Unset): native GeoJSON / records format options
        alertmanager (CustomSourceFormatsAlertmanager | Unset): Alertmanager v4 webhook options; `severity` and
            `alertname` labels are also copied to top-level payload fields (059)
        cloudevents (CustomSourceFormatsCloudevents | Unset): CloudEvents options
        cap (CustomSourceFormatsCap | Unset): CAP 1.2 options
        mapping (None | Unset): Not accepted since 059 (400): the mapping is the top-level `mapping` member of
            CustomSourceInput (a RecordMapping). The 024 JSON-Pointer shape was replaced.
        owntracks (CustomSourceFormatsOwntracks | Unset): OwnTracks options (039)
        osmand (CustomSourceFormatsOsmand | Unset): OsmAnd/Traccar options (039)
        overland (CustomSourceFormatsOverland | Unset): Overland options (039)
        gpslogger (CustomSourceFormatsGpslogger | Unset): GPSLogger options (039)
    """

    native: CustomSourceFormatsNative | Unset = UNSET
    alertmanager: CustomSourceFormatsAlertmanager | Unset = UNSET
    cloudevents: CustomSourceFormatsCloudevents | Unset = UNSET
    cap: CustomSourceFormatsCap | Unset = UNSET
    mapping: None | Unset = UNSET
    owntracks: CustomSourceFormatsOwntracks | Unset = UNSET
    osmand: CustomSourceFormatsOsmand | Unset = UNSET
    overland: CustomSourceFormatsOverland | Unset = UNSET
    gpslogger: CustomSourceFormatsGpslogger | Unset = UNSET
    additional_properties: dict[str, CustomSourceFormatsAdditionalProperty] = (
        _attrs_field(init=False, factory=dict)
    )

    def to_dict(self) -> dict[str, Any]:
        native: dict[str, Any] | Unset = UNSET
        if not isinstance(self.native, Unset):
            native = self.native.to_dict()

        alertmanager: dict[str, Any] | Unset = UNSET
        if not isinstance(self.alertmanager, Unset):
            alertmanager = self.alertmanager.to_dict()

        cloudevents: dict[str, Any] | Unset = UNSET
        if not isinstance(self.cloudevents, Unset):
            cloudevents = self.cloudevents.to_dict()

        cap: dict[str, Any] | Unset = UNSET
        if not isinstance(self.cap, Unset):
            cap = self.cap.to_dict()

        mapping = self.mapping

        owntracks: dict[str, Any] | Unset = UNSET
        if not isinstance(self.owntracks, Unset):
            owntracks = self.owntracks.to_dict()

        osmand: dict[str, Any] | Unset = UNSET
        if not isinstance(self.osmand, Unset):
            osmand = self.osmand.to_dict()

        overland: dict[str, Any] | Unset = UNSET
        if not isinstance(self.overland, Unset):
            overland = self.overland.to_dict()

        gpslogger: dict[str, Any] | Unset = UNSET
        if not isinstance(self.gpslogger, Unset):
            gpslogger = self.gpslogger.to_dict()

        field_dict: dict[str, Any] = {}
        for prop_name, prop in self.additional_properties.items():
            field_dict[prop_name] = prop.to_dict()

        field_dict.update({})
        if native is not UNSET:
            field_dict["native"] = native
        if alertmanager is not UNSET:
            field_dict["alertmanager"] = alertmanager
        if cloudevents is not UNSET:
            field_dict["cloudevents"] = cloudevents
        if cap is not UNSET:
            field_dict["cap"] = cap
        if mapping is not UNSET:
            field_dict["mapping"] = mapping
        if owntracks is not UNSET:
            field_dict["owntracks"] = owntracks
        if osmand is not UNSET:
            field_dict["osmand"] = osmand
        if overland is not UNSET:
            field_dict["overland"] = overland
        if gpslogger is not UNSET:
            field_dict["gpslogger"] = gpslogger

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.custom_source_formats_additional_property import (
            CustomSourceFormatsAdditionalProperty,
        )
        from ..models.custom_source_formats_alertmanager import (
            CustomSourceFormatsAlertmanager,
        )
        from ..models.custom_source_formats_cap import CustomSourceFormatsCap
        from ..models.custom_source_formats_cloudevents import (
            CustomSourceFormatsCloudevents,
        )
        from ..models.custom_source_formats_gpslogger import (
            CustomSourceFormatsGpslogger,
        )
        from ..models.custom_source_formats_native import CustomSourceFormatsNative
        from ..models.custom_source_formats_osmand import CustomSourceFormatsOsmand
        from ..models.custom_source_formats_overland import CustomSourceFormatsOverland
        from ..models.custom_source_formats_owntracks import (
            CustomSourceFormatsOwntracks,
        )

        d = dict(src_dict)
        _native = d.pop("native", UNSET)
        native: CustomSourceFormatsNative | Unset
        if isinstance(_native, Unset):
            native = UNSET
        else:
            native = CustomSourceFormatsNative.from_dict(_native)

        _alertmanager = d.pop("alertmanager", UNSET)
        alertmanager: CustomSourceFormatsAlertmanager | Unset
        if isinstance(_alertmanager, Unset):
            alertmanager = UNSET
        else:
            alertmanager = CustomSourceFormatsAlertmanager.from_dict(_alertmanager)

        _cloudevents = d.pop("cloudevents", UNSET)
        cloudevents: CustomSourceFormatsCloudevents | Unset
        if isinstance(_cloudevents, Unset):
            cloudevents = UNSET
        else:
            cloudevents = CustomSourceFormatsCloudevents.from_dict(_cloudevents)

        _cap = d.pop("cap", UNSET)
        cap: CustomSourceFormatsCap | Unset
        if isinstance(_cap, Unset):
            cap = UNSET
        else:
            cap = CustomSourceFormatsCap.from_dict(_cap)

        mapping = d.pop("mapping", UNSET)

        _owntracks = d.pop("owntracks", UNSET)
        owntracks: CustomSourceFormatsOwntracks | Unset
        if isinstance(_owntracks, Unset):
            owntracks = UNSET
        else:
            owntracks = CustomSourceFormatsOwntracks.from_dict(_owntracks)

        _osmand = d.pop("osmand", UNSET)
        osmand: CustomSourceFormatsOsmand | Unset
        if isinstance(_osmand, Unset):
            osmand = UNSET
        else:
            osmand = CustomSourceFormatsOsmand.from_dict(_osmand)

        _overland = d.pop("overland", UNSET)
        overland: CustomSourceFormatsOverland | Unset
        if isinstance(_overland, Unset):
            overland = UNSET
        else:
            overland = CustomSourceFormatsOverland.from_dict(_overland)

        _gpslogger = d.pop("gpslogger", UNSET)
        gpslogger: CustomSourceFormatsGpslogger | Unset
        if isinstance(_gpslogger, Unset):
            gpslogger = UNSET
        else:
            gpslogger = CustomSourceFormatsGpslogger.from_dict(_gpslogger)

        custom_source_formats = cls(
            native=native,
            alertmanager=alertmanager,
            cloudevents=cloudevents,
            cap=cap,
            mapping=mapping,
            owntracks=owntracks,
            osmand=osmand,
            overland=overland,
            gpslogger=gpslogger,
        )

        additional_properties = {}
        for prop_name, prop_dict in d.items():
            additional_property = CustomSourceFormatsAdditionalProperty.from_dict(
                prop_dict
            )

            additional_properties[prop_name] = additional_property

        custom_source_formats.additional_properties = additional_properties
        return custom_source_formats

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> CustomSourceFormatsAdditionalProperty:
        return self.additional_properties[key]

    def __setitem__(
        self, key: str, value: CustomSourceFormatsAdditionalProperty
    ) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
