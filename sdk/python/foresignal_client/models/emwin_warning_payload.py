from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.emwin_warning_payload_duplicate_of_type_0 import (
        EmwinWarningPayloadDuplicateOfType0,
    )


T = TypeVar("T", bound="EmwinWarningPayload")


@_attrs_define
class EmwinWarningPayload:
    """Kind `emwin-warning` (externalId `{office}.{phen}.{sig}.{etn}`): a P-VTEC warning with polygon received via EMWIN;
    never more authoritative than nws-alerts

        Attributes:
            action (str):  Example: NEW.
            phenomena (str):  Example: TO.
            significance (str):  Example: W.
            office (str):  Example: KFWD.
            etn (int):
            valid_from (datetime.datetime):
            valid_until (datetime.datetime):
            fallback (bool):
            authoritative_source (Literal['nws-alerts']):
            headline (None | str | Unset):
            duplicate_of (EmwinWarningPayloadDuplicateOfType0 | None | Unset):
            received_via (Literal['Received via GOES HRIT EMWIN'] | Unset):
    """

    action: str
    phenomena: str
    significance: str
    office: str
    etn: int
    valid_from: datetime.datetime
    valid_until: datetime.datetime
    fallback: bool
    authoritative_source: Literal["nws-alerts"]
    headline: None | str | Unset = UNSET
    duplicate_of: EmwinWarningPayloadDuplicateOfType0 | None | Unset = UNSET
    received_via: Literal["Received via GOES HRIT EMWIN"] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.emwin_warning_payload_duplicate_of_type_0 import (
            EmwinWarningPayloadDuplicateOfType0,
        )

        action = self.action

        phenomena = self.phenomena

        significance = self.significance

        office = self.office

        etn = self.etn

        valid_from = self.valid_from.isoformat()

        valid_until = self.valid_until.isoformat()

        fallback = self.fallback

        authoritative_source = self.authoritative_source

        headline: None | str | Unset
        if isinstance(self.headline, Unset):
            headline = UNSET
        else:
            headline = self.headline

        duplicate_of: dict[str, Any] | None | Unset
        if isinstance(self.duplicate_of, Unset):
            duplicate_of = UNSET
        elif isinstance(self.duplicate_of, EmwinWarningPayloadDuplicateOfType0):
            duplicate_of = self.duplicate_of.to_dict()
        else:
            duplicate_of = self.duplicate_of

        received_via = self.received_via

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "action": action,
                "phenomena": phenomena,
                "significance": significance,
                "office": office,
                "etn": etn,
                "validFrom": valid_from,
                "validUntil": valid_until,
                "fallback": fallback,
                "authoritativeSource": authoritative_source,
            }
        )
        if headline is not UNSET:
            field_dict["headline"] = headline
        if duplicate_of is not UNSET:
            field_dict["duplicateOf"] = duplicate_of
        if received_via is not UNSET:
            field_dict["receivedVia"] = received_via

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.emwin_warning_payload_duplicate_of_type_0 import (
            EmwinWarningPayloadDuplicateOfType0,
        )

        d = dict(src_dict)
        action = d.pop("action")

        phenomena = d.pop("phenomena")

        significance = d.pop("significance")

        office = d.pop("office")

        etn = d.pop("etn")

        valid_from = datetime.datetime.fromisoformat(d.pop("validFrom"))

        valid_until = datetime.datetime.fromisoformat(d.pop("validUntil"))

        fallback = d.pop("fallback")

        authoritative_source = cast(Literal["nws-alerts"], d.pop("authoritativeSource"))
        if authoritative_source != "nws-alerts":
            raise ValueError(
                f"authoritativeSource must match const 'nws-alerts', got '{authoritative_source}'"
            )

        def _parse_headline(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        headline = _parse_headline(d.pop("headline", UNSET))

        def _parse_duplicate_of(
            data: object,
        ) -> EmwinWarningPayloadDuplicateOfType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                duplicate_of_type_0 = EmwinWarningPayloadDuplicateOfType0.from_dict(
                    data
                )

                return duplicate_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EmwinWarningPayloadDuplicateOfType0 | None | Unset, data)

        duplicate_of = _parse_duplicate_of(d.pop("duplicateOf", UNSET))

        received_via = cast(
            Literal["Received via GOES HRIT EMWIN"] | Unset, d.pop("receivedVia", UNSET)
        )
        if received_via != "Received via GOES HRIT EMWIN" and not isinstance(
            received_via, Unset
        ):
            raise ValueError(
                f"receivedVia must match const 'Received via GOES HRIT EMWIN', got '{received_via}'"
            )

        emwin_warning_payload = cls(
            action=action,
            phenomena=phenomena,
            significance=significance,
            office=office,
            etn=etn,
            valid_from=valid_from,
            valid_until=valid_until,
            fallback=fallback,
            authoritative_source=authoritative_source,
            headline=headline,
            duplicate_of=duplicate_of,
            received_via=received_via,
        )

        emwin_warning_payload.additional_properties = d
        return emwin_warning_payload

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
