from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EmwinProductPayload")


@_attrs_define
class EmwinProductPayload:
    """Kind `emwin-product`: an EMWIN text product received via GOES HRIT (untrusted text, length-capped)

    Attributes:
        heading (str): WMO heading TTAAii CCCC DDHHMM
        awips_id (str):
        issued_at (datetime.datetime):
        text (str):
        office (str | Unset):
    """

    heading: str
    awips_id: str
    issued_at: datetime.datetime
    text: str
    office: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        heading = self.heading

        awips_id = self.awips_id

        issued_at = self.issued_at.isoformat()

        text = self.text

        office = self.office

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "heading": heading,
                "awipsId": awips_id,
                "issuedAt": issued_at,
                "text": text,
            }
        )
        if office is not UNSET:
            field_dict["office"] = office

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        heading = d.pop("heading")

        awips_id = d.pop("awipsId")

        issued_at = datetime.datetime.fromisoformat(d.pop("issuedAt"))

        text = d.pop("text")

        office = d.pop("office", UNSET)

        emwin_product_payload = cls(
            heading=heading,
            awips_id=awips_id,
            issued_at=issued_at,
            text=text,
            office=office,
        )

        emwin_product_payload.additional_properties = d
        return emwin_product_payload

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
