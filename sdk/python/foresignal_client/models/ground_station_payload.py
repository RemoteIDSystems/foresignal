from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ground_station_payload_satdump_mode import GroundStationPayloadSatdumpMode
from ..models.ground_station_payload_state import GroundStationPayloadState
from ..types import UNSET, Unset

T = TypeVar("T", bound="GroundStationPayload")


@_attrs_define
class GroundStationPayload:
    """Payload of `ground-station` records (satdump-{station}, externalId = station)

    Attributes:
        state (GroundStationPayloadState):
        title (str):
        current_target (None | str | Unset):
        frequency_hz (int | None | Unset):
        az (int | None | Unset): 1° steps
        el (int | None | Unset): 1° steps
        snr_db (int | None | Unset): 1 dB steps
        next_aos (datetime.datetime | None | Unset):
        next_los (datetime.datetime | None | Unset):
        rotator_engaged (bool | Unset):
        satdump_mode (GroundStationPayloadSatdumpMode | Unset):
    """

    state: GroundStationPayloadState
    title: str
    current_target: None | str | Unset = UNSET
    frequency_hz: int | None | Unset = UNSET
    az: int | None | Unset = UNSET
    el: int | None | Unset = UNSET
    snr_db: int | None | Unset = UNSET
    next_aos: datetime.datetime | None | Unset = UNSET
    next_los: datetime.datetime | None | Unset = UNSET
    rotator_engaged: bool | Unset = UNSET
    satdump_mode: GroundStationPayloadSatdumpMode | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        state = self.state.value

        title = self.title

        current_target: None | str | Unset
        if isinstance(self.current_target, Unset):
            current_target = UNSET
        else:
            current_target = self.current_target

        frequency_hz: int | None | Unset
        if isinstance(self.frequency_hz, Unset):
            frequency_hz = UNSET
        else:
            frequency_hz = self.frequency_hz

        az: int | None | Unset
        if isinstance(self.az, Unset):
            az = UNSET
        else:
            az = self.az

        el: int | None | Unset
        if isinstance(self.el, Unset):
            el = UNSET
        else:
            el = self.el

        snr_db: int | None | Unset
        if isinstance(self.snr_db, Unset):
            snr_db = UNSET
        else:
            snr_db = self.snr_db

        next_aos: None | str | Unset
        if isinstance(self.next_aos, Unset):
            next_aos = UNSET
        elif isinstance(self.next_aos, datetime.datetime):
            next_aos = self.next_aos.isoformat()
        else:
            next_aos = self.next_aos

        next_los: None | str | Unset
        if isinstance(self.next_los, Unset):
            next_los = UNSET
        elif isinstance(self.next_los, datetime.datetime):
            next_los = self.next_los.isoformat()
        else:
            next_los = self.next_los

        rotator_engaged = self.rotator_engaged

        satdump_mode: str | Unset = UNSET
        if not isinstance(self.satdump_mode, Unset):
            satdump_mode = self.satdump_mode.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "state": state,
                "title": title,
            }
        )
        if current_target is not UNSET:
            field_dict["currentTarget"] = current_target
        if frequency_hz is not UNSET:
            field_dict["frequencyHz"] = frequency_hz
        if az is not UNSET:
            field_dict["az"] = az
        if el is not UNSET:
            field_dict["el"] = el
        if snr_db is not UNSET:
            field_dict["snrDb"] = snr_db
        if next_aos is not UNSET:
            field_dict["nextAos"] = next_aos
        if next_los is not UNSET:
            field_dict["nextLos"] = next_los
        if rotator_engaged is not UNSET:
            field_dict["rotatorEngaged"] = rotator_engaged
        if satdump_mode is not UNSET:
            field_dict["satdumpMode"] = satdump_mode

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        state = GroundStationPayloadState(d.pop("state"))

        title = d.pop("title")

        def _parse_current_target(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        current_target = _parse_current_target(d.pop("currentTarget", UNSET))

        def _parse_frequency_hz(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        frequency_hz = _parse_frequency_hz(d.pop("frequencyHz", UNSET))

        def _parse_az(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        az = _parse_az(d.pop("az", UNSET))

        def _parse_el(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        el = _parse_el(d.pop("el", UNSET))

        def _parse_snr_db(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        snr_db = _parse_snr_db(d.pop("snrDb", UNSET))

        def _parse_next_aos(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                next_aos_type_0 = datetime.datetime.fromisoformat(data)

                return next_aos_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        next_aos = _parse_next_aos(d.pop("nextAos", UNSET))

        def _parse_next_los(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                next_los_type_0 = datetime.datetime.fromisoformat(data)

                return next_los_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        next_los = _parse_next_los(d.pop("nextLos", UNSET))

        rotator_engaged = d.pop("rotatorEngaged", UNSET)

        _satdump_mode = d.pop("satdumpMode", UNSET)
        satdump_mode: GroundStationPayloadSatdumpMode | Unset
        if isinstance(_satdump_mode, Unset):
            satdump_mode = UNSET
        else:
            satdump_mode = GroundStationPayloadSatdumpMode(_satdump_mode)

        ground_station_payload = cls(
            state=state,
            title=title,
            current_target=current_target,
            frequency_hz=frequency_hz,
            az=az,
            el=el,
            snr_db=snr_db,
            next_aos=next_aos,
            next_los=next_los,
            rotator_engaged=rotator_engaged,
            satdump_mode=satdump_mode,
        )

        ground_station_payload.additional_properties = d
        return ground_station_payload

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
