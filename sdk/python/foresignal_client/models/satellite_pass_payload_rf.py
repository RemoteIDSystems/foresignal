from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.satellite_pass_payload_rf_snr_by_elevation_item import (
        SatellitePassPayloadRfSnrByElevationItem,
    )


T = TypeVar("T", bound="SatellitePassPayloadRf")


@_attrs_define
class SatellitePassPayloadRf:
    """RF session aggregate at LOS

    Attributes:
        snr_max_db (float | Unset):
        snr_mean_db (float | Unset):
        ber_min (float | Unset):
        ber_mean (float | Unset):
        synced_fraction (float | Unset):
        first_lock_at (datetime.datetime | None | Unset):
        last_lock_at (datetime.datetime | None | Unset):
        first_lock_elevation_deg (float | Unset):
        last_lock_elevation_deg (float | Unset):
        snr_by_elevation (list[SatellitePassPayloadRfSnrByElevationItem] | Unset): About 60 points
    """

    snr_max_db: float | Unset = UNSET
    snr_mean_db: float | Unset = UNSET
    ber_min: float | Unset = UNSET
    ber_mean: float | Unset = UNSET
    synced_fraction: float | Unset = UNSET
    first_lock_at: datetime.datetime | None | Unset = UNSET
    last_lock_at: datetime.datetime | None | Unset = UNSET
    first_lock_elevation_deg: float | Unset = UNSET
    last_lock_elevation_deg: float | Unset = UNSET
    snr_by_elevation: list[SatellitePassPayloadRfSnrByElevationItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        snr_max_db = self.snr_max_db

        snr_mean_db = self.snr_mean_db

        ber_min = self.ber_min

        ber_mean = self.ber_mean

        synced_fraction = self.synced_fraction

        first_lock_at: None | str | Unset
        if isinstance(self.first_lock_at, Unset):
            first_lock_at = UNSET
        elif isinstance(self.first_lock_at, datetime.datetime):
            first_lock_at = self.first_lock_at.isoformat()
        else:
            first_lock_at = self.first_lock_at

        last_lock_at: None | str | Unset
        if isinstance(self.last_lock_at, Unset):
            last_lock_at = UNSET
        elif isinstance(self.last_lock_at, datetime.datetime):
            last_lock_at = self.last_lock_at.isoformat()
        else:
            last_lock_at = self.last_lock_at

        first_lock_elevation_deg = self.first_lock_elevation_deg

        last_lock_elevation_deg = self.last_lock_elevation_deg

        snr_by_elevation: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.snr_by_elevation, Unset):
            snr_by_elevation = []
            for snr_by_elevation_item_data in self.snr_by_elevation:
                snr_by_elevation_item = snr_by_elevation_item_data.to_dict()
                snr_by_elevation.append(snr_by_elevation_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if snr_max_db is not UNSET:
            field_dict["snrMaxDb"] = snr_max_db
        if snr_mean_db is not UNSET:
            field_dict["snrMeanDb"] = snr_mean_db
        if ber_min is not UNSET:
            field_dict["berMin"] = ber_min
        if ber_mean is not UNSET:
            field_dict["berMean"] = ber_mean
        if synced_fraction is not UNSET:
            field_dict["syncedFraction"] = synced_fraction
        if first_lock_at is not UNSET:
            field_dict["firstLockAt"] = first_lock_at
        if last_lock_at is not UNSET:
            field_dict["lastLockAt"] = last_lock_at
        if first_lock_elevation_deg is not UNSET:
            field_dict["firstLockElevationDeg"] = first_lock_elevation_deg
        if last_lock_elevation_deg is not UNSET:
            field_dict["lastLockElevationDeg"] = last_lock_elevation_deg
        if snr_by_elevation is not UNSET:
            field_dict["snrByElevation"] = snr_by_elevation

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.satellite_pass_payload_rf_snr_by_elevation_item import (
            SatellitePassPayloadRfSnrByElevationItem,
        )

        d = dict(src_dict)
        snr_max_db = d.pop("snrMaxDb", UNSET)

        snr_mean_db = d.pop("snrMeanDb", UNSET)

        ber_min = d.pop("berMin", UNSET)

        ber_mean = d.pop("berMean", UNSET)

        synced_fraction = d.pop("syncedFraction", UNSET)

        def _parse_first_lock_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                first_lock_at_type_0 = datetime.datetime.fromisoformat(data)

                return first_lock_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        first_lock_at = _parse_first_lock_at(d.pop("firstLockAt", UNSET))

        def _parse_last_lock_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_lock_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_lock_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_lock_at = _parse_last_lock_at(d.pop("lastLockAt", UNSET))

        first_lock_elevation_deg = d.pop("firstLockElevationDeg", UNSET)

        last_lock_elevation_deg = d.pop("lastLockElevationDeg", UNSET)

        _snr_by_elevation = d.pop("snrByElevation", UNSET)
        snr_by_elevation: list[SatellitePassPayloadRfSnrByElevationItem] | Unset = UNSET
        if _snr_by_elevation is not UNSET:
            snr_by_elevation = []
            for snr_by_elevation_item_data in _snr_by_elevation:
                snr_by_elevation_item = (
                    SatellitePassPayloadRfSnrByElevationItem.from_dict(
                        snr_by_elevation_item_data
                    )
                )

                snr_by_elevation.append(snr_by_elevation_item)

        satellite_pass_payload_rf = cls(
            snr_max_db=snr_max_db,
            snr_mean_db=snr_mean_db,
            ber_min=ber_min,
            ber_mean=ber_mean,
            synced_fraction=synced_fraction,
            first_lock_at=first_lock_at,
            last_lock_at=last_lock_at,
            first_lock_elevation_deg=first_lock_elevation_deg,
            last_lock_elevation_deg=last_lock_elevation_deg,
            snr_by_elevation=snr_by_elevation,
        )

        satellite_pass_payload_rf.additional_properties = d
        return satellite_pass_payload_rf

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
