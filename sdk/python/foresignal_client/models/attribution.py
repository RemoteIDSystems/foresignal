from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="Attribution")


@_attrs_define
class Attribution:
    """Structured attribution clients must render for a product or derived output (Weather-Imagery-Design §6.10)

    Attributes:
        text (str): Plain-text attribution line Example: MRMS: NOAA/NSSL (public domain).
        licence (str): Licence identifier Example: US-PD.
        required_display (bool): True when the licence requires the attribution to be visible on the map or image
        html (None | str | Unset): HTML form of the attribution
        url (None | str | Unset):
        licence_url (None | str | Unset):
        source_id (str | Unset):
        terms_url (None | str | Unset):
        count (int | Unset): Records in the response from this source under this licence
    """

    text: str
    licence: str
    required_display: bool
    html: None | str | Unset = UNSET
    url: None | str | Unset = UNSET
    licence_url: None | str | Unset = UNSET
    source_id: str | Unset = UNSET
    terms_url: None | str | Unset = UNSET
    count: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        text = self.text

        licence = self.licence

        required_display = self.required_display

        html: None | str | Unset
        if isinstance(self.html, Unset):
            html = UNSET
        else:
            html = self.html

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        licence_url: None | str | Unset
        if isinstance(self.licence_url, Unset):
            licence_url = UNSET
        else:
            licence_url = self.licence_url

        source_id = self.source_id

        terms_url: None | str | Unset
        if isinstance(self.terms_url, Unset):
            terms_url = UNSET
        else:
            terms_url = self.terms_url

        count = self.count

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "text": text,
                "licence": licence,
                "requiredDisplay": required_display,
            }
        )
        if html is not UNSET:
            field_dict["html"] = html
        if url is not UNSET:
            field_dict["url"] = url
        if licence_url is not UNSET:
            field_dict["licenceUrl"] = licence_url
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if terms_url is not UNSET:
            field_dict["termsUrl"] = terms_url
        if count is not UNSET:
            field_dict["count"] = count

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        text = d.pop("text")

        licence = d.pop("licence")

        required_display = d.pop("requiredDisplay")

        def _parse_html(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        html = _parse_html(d.pop("html", UNSET))

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        def _parse_licence_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        licence_url = _parse_licence_url(d.pop("licenceUrl", UNSET))

        source_id = d.pop("sourceId", UNSET)

        def _parse_terms_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        terms_url = _parse_terms_url(d.pop("termsUrl", UNSET))

        count = d.pop("count", UNSET)

        attribution = cls(
            text=text,
            licence=licence,
            required_display=required_display,
            html=html,
            url=url,
            licence_url=licence_url,
            source_id=source_id,
            terms_url=terms_url,
            count=count,
        )

        attribution.additional_properties = d
        return attribution

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
