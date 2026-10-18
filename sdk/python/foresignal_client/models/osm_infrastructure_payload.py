from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.osm_infrastructure_payload_osm_type_type_1 import (
    OsmInfrastructurePayloadOsmTypeType1,
)
from ..models.osm_infrastructure_payload_osm_type_type_2_type_1 import (
    OsmInfrastructurePayloadOsmTypeType2Type1,
)
from ..models.osm_infrastructure_payload_osm_type_type_3_type_1 import (
    OsmInfrastructurePayloadOsmTypeType3Type1,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="OsmInfrastructurePayload")


@_attrs_define
class OsmInfrastructurePayload:
    """Payload of `osm-infrastructure` records (allowlisted tags only; ODbL-1.0)

    Attributes:
        name (None | str | Unset):
        operator (None | str | Unset):
        voltage_kv (float | None | Unset):
        capacity_mw (float | None | Unset):
        beds (int | None | Unset):
        emergency (None | str | Unset):
        iata (None | str | Unset):
        icao (None | str | Unset):
        ref (None | str | Unset):
        osm_type (None | OsmInfrastructurePayloadOsmTypeType1 | OsmInfrastructurePayloadOsmTypeType2Type1 |
            OsmInfrastructurePayloadOsmTypeType3Type1 | Unset):
        osm_version (int | None | Unset):
    """

    name: None | str | Unset = UNSET
    operator: None | str | Unset = UNSET
    voltage_kv: float | None | Unset = UNSET
    capacity_mw: float | None | Unset = UNSET
    beds: int | None | Unset = UNSET
    emergency: None | str | Unset = UNSET
    iata: None | str | Unset = UNSET
    icao: None | str | Unset = UNSET
    ref: None | str | Unset = UNSET
    osm_type: (
        None
        | OsmInfrastructurePayloadOsmTypeType1
        | OsmInfrastructurePayloadOsmTypeType2Type1
        | OsmInfrastructurePayloadOsmTypeType3Type1
        | Unset
    ) = UNSET
    osm_version: int | None | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        operator: None | str | Unset
        if isinstance(self.operator, Unset):
            operator = UNSET
        else:
            operator = self.operator

        voltage_kv: float | None | Unset
        if isinstance(self.voltage_kv, Unset):
            voltage_kv = UNSET
        else:
            voltage_kv = self.voltage_kv

        capacity_mw: float | None | Unset
        if isinstance(self.capacity_mw, Unset):
            capacity_mw = UNSET
        else:
            capacity_mw = self.capacity_mw

        beds: int | None | Unset
        if isinstance(self.beds, Unset):
            beds = UNSET
        else:
            beds = self.beds

        emergency: None | str | Unset
        if isinstance(self.emergency, Unset):
            emergency = UNSET
        else:
            emergency = self.emergency

        iata: None | str | Unset
        if isinstance(self.iata, Unset):
            iata = UNSET
        else:
            iata = self.iata

        icao: None | str | Unset
        if isinstance(self.icao, Unset):
            icao = UNSET
        else:
            icao = self.icao

        ref: None | str | Unset
        if isinstance(self.ref, Unset):
            ref = UNSET
        else:
            ref = self.ref

        osm_type: None | str | Unset
        if isinstance(self.osm_type, Unset):
            osm_type = UNSET
        elif (
            isinstance(self.osm_type, OsmInfrastructurePayloadOsmTypeType1)
            or isinstance(self.osm_type, OsmInfrastructurePayloadOsmTypeType2Type1)
            or isinstance(self.osm_type, OsmInfrastructurePayloadOsmTypeType3Type1)
        ):
            osm_type = self.osm_type.value
        else:
            osm_type = self.osm_type

        osm_version: int | None | Unset
        if isinstance(self.osm_version, Unset):
            osm_version = UNSET
        else:
            osm_version = self.osm_version

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if name is not UNSET:
            field_dict["name"] = name
        if operator is not UNSET:
            field_dict["operator"] = operator
        if voltage_kv is not UNSET:
            field_dict["voltageKv"] = voltage_kv
        if capacity_mw is not UNSET:
            field_dict["capacityMw"] = capacity_mw
        if beds is not UNSET:
            field_dict["beds"] = beds
        if emergency is not UNSET:
            field_dict["emergency"] = emergency
        if iata is not UNSET:
            field_dict["iata"] = iata
        if icao is not UNSET:
            field_dict["icao"] = icao
        if ref is not UNSET:
            field_dict["ref"] = ref
        if osm_type is not UNSET:
            field_dict["osmType"] = osm_type
        if osm_version is not UNSET:
            field_dict["osmVersion"] = osm_version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        def _parse_operator(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        operator = _parse_operator(d.pop("operator", UNSET))

        def _parse_voltage_kv(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        voltage_kv = _parse_voltage_kv(d.pop("voltageKv", UNSET))

        def _parse_capacity_mw(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        capacity_mw = _parse_capacity_mw(d.pop("capacityMw", UNSET))

        def _parse_beds(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        beds = _parse_beds(d.pop("beds", UNSET))

        def _parse_emergency(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        emergency = _parse_emergency(d.pop("emergency", UNSET))

        def _parse_iata(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        iata = _parse_iata(d.pop("iata", UNSET))

        def _parse_icao(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        icao = _parse_icao(d.pop("icao", UNSET))

        def _parse_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ref = _parse_ref(d.pop("ref", UNSET))

        def _parse_osm_type(
            data: object,
        ) -> (
            None
            | OsmInfrastructurePayloadOsmTypeType1
            | OsmInfrastructurePayloadOsmTypeType2Type1
            | OsmInfrastructurePayloadOsmTypeType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                osm_type_type_1 = OsmInfrastructurePayloadOsmTypeType1(data)

                return osm_type_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                osm_type_type_2_type_1 = OsmInfrastructurePayloadOsmTypeType2Type1(data)

                return osm_type_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                osm_type_type_3_type_1 = OsmInfrastructurePayloadOsmTypeType3Type1(data)

                return osm_type_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | OsmInfrastructurePayloadOsmTypeType1
                | OsmInfrastructurePayloadOsmTypeType2Type1
                | OsmInfrastructurePayloadOsmTypeType3Type1
                | Unset,
                data,
            )

        osm_type = _parse_osm_type(d.pop("osmType", UNSET))

        def _parse_osm_version(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        osm_version = _parse_osm_version(d.pop("osmVersion", UNSET))

        osm_infrastructure_payload = cls(
            name=name,
            operator=operator,
            voltage_kv=voltage_kv,
            capacity_mw=capacity_mw,
            beds=beds,
            emergency=emergency,
            iata=iata,
            icao=icao,
            ref=ref,
            osm_type=osm_type,
            osm_version=osm_version,
        )

        return osm_infrastructure_payload
