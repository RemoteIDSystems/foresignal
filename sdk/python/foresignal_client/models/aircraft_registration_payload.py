from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

T = TypeVar("T", bound="AircraftRegistrationPayload")


@_attrs_define
class AircraftRegistrationPayload:
    """Payload of `faa-registry` records (kind `aircraft-registration`). Allowlisted fields only: owner names and addresses
    are never stored.

        Attributes:
            n_number (str):  Example: N123AB.
            serial (str | Unset):
            mfr_mdl_code (str | Unset):
            manufacturer (None | str | Unset):
            model (None | str | Unset):
            type_aircraft (None | str | Unset):
            type_engine (None | str | Unset):
            engines (int | None | Unset):
            seats (int | None | Unset):
            weight_class (None | str | Unset):
            year_mfr (int | None | Unset):
            registrant_type (None | str | Unset): Decoded registrant type (individual, partnership, corporation, co-owned,
                government, llc, non-citizen-corporation, non-citizen-co-owned)
            cert_issue_date (datetime.date | None | Unset):
            airworthiness_date (datetime.date | None | Unset):
            expiration_date (datetime.date | None | Unset):
            status_code (None | str | Unset):
            mode_s_hex (None | str | Unset): Lowercase ICAO 24-bit hex
            fractional_owner (bool | None | Unset):
            kit_mfr (None | str | Unset):
            kit_model (None | str | Unset):
            name (None | str | Unset): Registrant name, present only when the source param `nonIndividualOwnerNames` is on
                and the registrant is a corporation, government, LLC or non-citizen corporation
    """

    n_number: str
    serial: str | Unset = UNSET
    mfr_mdl_code: str | Unset = UNSET
    manufacturer: None | str | Unset = UNSET
    model: None | str | Unset = UNSET
    type_aircraft: None | str | Unset = UNSET
    type_engine: None | str | Unset = UNSET
    engines: int | None | Unset = UNSET
    seats: int | None | Unset = UNSET
    weight_class: None | str | Unset = UNSET
    year_mfr: int | None | Unset = UNSET
    registrant_type: None | str | Unset = UNSET
    cert_issue_date: datetime.date | None | Unset = UNSET
    airworthiness_date: datetime.date | None | Unset = UNSET
    expiration_date: datetime.date | None | Unset = UNSET
    status_code: None | str | Unset = UNSET
    mode_s_hex: None | str | Unset = UNSET
    fractional_owner: bool | None | Unset = UNSET
    kit_mfr: None | str | Unset = UNSET
    kit_model: None | str | Unset = UNSET
    name: None | str | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        n_number = self.n_number

        serial = self.serial

        mfr_mdl_code = self.mfr_mdl_code

        manufacturer: None | str | Unset
        if isinstance(self.manufacturer, Unset):
            manufacturer = UNSET
        else:
            manufacturer = self.manufacturer

        model: None | str | Unset
        if isinstance(self.model, Unset):
            model = UNSET
        else:
            model = self.model

        type_aircraft: None | str | Unset
        if isinstance(self.type_aircraft, Unset):
            type_aircraft = UNSET
        else:
            type_aircraft = self.type_aircraft

        type_engine: None | str | Unset
        if isinstance(self.type_engine, Unset):
            type_engine = UNSET
        else:
            type_engine = self.type_engine

        engines: int | None | Unset
        if isinstance(self.engines, Unset):
            engines = UNSET
        else:
            engines = self.engines

        seats: int | None | Unset
        if isinstance(self.seats, Unset):
            seats = UNSET
        else:
            seats = self.seats

        weight_class: None | str | Unset
        if isinstance(self.weight_class, Unset):
            weight_class = UNSET
        else:
            weight_class = self.weight_class

        year_mfr: int | None | Unset
        if isinstance(self.year_mfr, Unset):
            year_mfr = UNSET
        else:
            year_mfr = self.year_mfr

        registrant_type: None | str | Unset
        if isinstance(self.registrant_type, Unset):
            registrant_type = UNSET
        else:
            registrant_type = self.registrant_type

        cert_issue_date: None | str | Unset
        if isinstance(self.cert_issue_date, Unset):
            cert_issue_date = UNSET
        elif isinstance(self.cert_issue_date, datetime.date):
            cert_issue_date = self.cert_issue_date.isoformat()
        else:
            cert_issue_date = self.cert_issue_date

        airworthiness_date: None | str | Unset
        if isinstance(self.airworthiness_date, Unset):
            airworthiness_date = UNSET
        elif isinstance(self.airworthiness_date, datetime.date):
            airworthiness_date = self.airworthiness_date.isoformat()
        else:
            airworthiness_date = self.airworthiness_date

        expiration_date: None | str | Unset
        if isinstance(self.expiration_date, Unset):
            expiration_date = UNSET
        elif isinstance(self.expiration_date, datetime.date):
            expiration_date = self.expiration_date.isoformat()
        else:
            expiration_date = self.expiration_date

        status_code: None | str | Unset
        if isinstance(self.status_code, Unset):
            status_code = UNSET
        else:
            status_code = self.status_code

        mode_s_hex: None | str | Unset
        if isinstance(self.mode_s_hex, Unset):
            mode_s_hex = UNSET
        else:
            mode_s_hex = self.mode_s_hex

        fractional_owner: bool | None | Unset
        if isinstance(self.fractional_owner, Unset):
            fractional_owner = UNSET
        else:
            fractional_owner = self.fractional_owner

        kit_mfr: None | str | Unset
        if isinstance(self.kit_mfr, Unset):
            kit_mfr = UNSET
        else:
            kit_mfr = self.kit_mfr

        kit_model: None | str | Unset
        if isinstance(self.kit_model, Unset):
            kit_model = UNSET
        else:
            kit_model = self.kit_model

        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "nNumber": n_number,
            }
        )
        if serial is not UNSET:
            field_dict["serial"] = serial
        if mfr_mdl_code is not UNSET:
            field_dict["mfrMdlCode"] = mfr_mdl_code
        if manufacturer is not UNSET:
            field_dict["manufacturer"] = manufacturer
        if model is not UNSET:
            field_dict["model"] = model
        if type_aircraft is not UNSET:
            field_dict["typeAircraft"] = type_aircraft
        if type_engine is not UNSET:
            field_dict["typeEngine"] = type_engine
        if engines is not UNSET:
            field_dict["engines"] = engines
        if seats is not UNSET:
            field_dict["seats"] = seats
        if weight_class is not UNSET:
            field_dict["weightClass"] = weight_class
        if year_mfr is not UNSET:
            field_dict["yearMfr"] = year_mfr
        if registrant_type is not UNSET:
            field_dict["registrantType"] = registrant_type
        if cert_issue_date is not UNSET:
            field_dict["certIssueDate"] = cert_issue_date
        if airworthiness_date is not UNSET:
            field_dict["airworthinessDate"] = airworthiness_date
        if expiration_date is not UNSET:
            field_dict["expirationDate"] = expiration_date
        if status_code is not UNSET:
            field_dict["statusCode"] = status_code
        if mode_s_hex is not UNSET:
            field_dict["modeSHex"] = mode_s_hex
        if fractional_owner is not UNSET:
            field_dict["fractionalOwner"] = fractional_owner
        if kit_mfr is not UNSET:
            field_dict["kitMfr"] = kit_mfr
        if kit_model is not UNSET:
            field_dict["kitModel"] = kit_model
        if name is not UNSET:
            field_dict["name"] = name

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        n_number = d.pop("nNumber")

        serial = d.pop("serial", UNSET)

        mfr_mdl_code = d.pop("mfrMdlCode", UNSET)

        def _parse_manufacturer(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manufacturer = _parse_manufacturer(d.pop("manufacturer", UNSET))

        def _parse_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        model = _parse_model(d.pop("model", UNSET))

        def _parse_type_aircraft(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        type_aircraft = _parse_type_aircraft(d.pop("typeAircraft", UNSET))

        def _parse_type_engine(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        type_engine = _parse_type_engine(d.pop("typeEngine", UNSET))

        def _parse_engines(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        engines = _parse_engines(d.pop("engines", UNSET))

        def _parse_seats(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        seats = _parse_seats(d.pop("seats", UNSET))

        def _parse_weight_class(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        weight_class = _parse_weight_class(d.pop("weightClass", UNSET))

        def _parse_year_mfr(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        year_mfr = _parse_year_mfr(d.pop("yearMfr", UNSET))

        def _parse_registrant_type(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        registrant_type = _parse_registrant_type(d.pop("registrantType", UNSET))

        def _parse_cert_issue_date(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                cert_issue_date_type_0 = datetime.date.fromisoformat(data)

                return cert_issue_date_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        cert_issue_date = _parse_cert_issue_date(d.pop("certIssueDate", UNSET))

        def _parse_airworthiness_date(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                airworthiness_date_type_0 = datetime.date.fromisoformat(data)

                return airworthiness_date_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        airworthiness_date = _parse_airworthiness_date(
            d.pop("airworthinessDate", UNSET)
        )

        def _parse_expiration_date(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                expiration_date_type_0 = datetime.date.fromisoformat(data)

                return expiration_date_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        expiration_date = _parse_expiration_date(d.pop("expirationDate", UNSET))

        def _parse_status_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        status_code = _parse_status_code(d.pop("statusCode", UNSET))

        def _parse_mode_s_hex(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        mode_s_hex = _parse_mode_s_hex(d.pop("modeSHex", UNSET))

        def _parse_fractional_owner(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        fractional_owner = _parse_fractional_owner(d.pop("fractionalOwner", UNSET))

        def _parse_kit_mfr(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        kit_mfr = _parse_kit_mfr(d.pop("kitMfr", UNSET))

        def _parse_kit_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        kit_model = _parse_kit_model(d.pop("kitModel", UNSET))

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        aircraft_registration_payload = cls(
            n_number=n_number,
            serial=serial,
            mfr_mdl_code=mfr_mdl_code,
            manufacturer=manufacturer,
            model=model,
            type_aircraft=type_aircraft,
            type_engine=type_engine,
            engines=engines,
            seats=seats,
            weight_class=weight_class,
            year_mfr=year_mfr,
            registrant_type=registrant_type,
            cert_issue_date=cert_issue_date,
            airworthiness_date=airworthiness_date,
            expiration_date=expiration_date,
            status_code=status_code,
            mode_s_hex=mode_s_hex,
            fractional_owner=fractional_owner,
            kit_mfr=kit_mfr,
            kit_model=kit_model,
            name=name,
        )

        return aircraft_registration_payload
