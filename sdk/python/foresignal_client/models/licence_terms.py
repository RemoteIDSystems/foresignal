from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.licence_terms_commercial_use import LicenceTermsCommercialUse
from ..models.licence_terms_redistribution import LicenceTermsRedistribution
from ..types import UNSET, Unset

T = TypeVar("T", bound="LicenceTerms")


@_attrs_define
class LicenceTerms:
    """Structured licence terms resolved for a source or a record version (109)

    Attributes:
        id (str): SPDX id where one exists (CC-BY-4.0, ODbL-1.0, CC0-1.0, CC-BY-NC-SA-4.0, CC-BY-SA-4.0, CC-BY-NC-4.0,
            PDDL-1.0, OGL-UK-3.0, NLOD-2.0, DL-DE-BY-2.0), else a slug (us-gov-public-domain, opensky-terms, acled-terms,
            aisstream-terms, unknown) Example: CC-BY-4.0.
        name (str):
        commercial_use (LicenceTermsCommercialUse): UNKNOWN is treated as FORBIDDEN by every egress guard
        share_alike (bool):
        attribution_required (bool):
        redistribution (LicenceTermsRedistribution): UNKNOWN is treated as LOCAL_ONLY by every egress guard
        attribution_text (None | str | Unset):
        terms_url (None | str | Unset):
    """

    id: str
    name: str
    commercial_use: LicenceTermsCommercialUse
    share_alike: bool
    attribution_required: bool
    redistribution: LicenceTermsRedistribution
    attribution_text: None | str | Unset = UNSET
    terms_url: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        name = self.name

        commercial_use = self.commercial_use.value

        share_alike = self.share_alike

        attribution_required = self.attribution_required

        redistribution = self.redistribution.value

        attribution_text: None | str | Unset
        if isinstance(self.attribution_text, Unset):
            attribution_text = UNSET
        else:
            attribution_text = self.attribution_text

        terms_url: None | str | Unset
        if isinstance(self.terms_url, Unset):
            terms_url = UNSET
        else:
            terms_url = self.terms_url

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "commercialUse": commercial_use,
                "shareAlike": share_alike,
                "attributionRequired": attribution_required,
                "redistribution": redistribution,
            }
        )
        if attribution_text is not UNSET:
            field_dict["attributionText"] = attribution_text
        if terms_url is not UNSET:
            field_dict["termsUrl"] = terms_url

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        commercial_use = LicenceTermsCommercialUse(d.pop("commercialUse"))

        share_alike = d.pop("shareAlike")

        attribution_required = d.pop("attributionRequired")

        redistribution = LicenceTermsRedistribution(d.pop("redistribution"))

        def _parse_attribution_text(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        attribution_text = _parse_attribution_text(d.pop("attributionText", UNSET))

        def _parse_terms_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        terms_url = _parse_terms_url(d.pop("termsUrl", UNSET))

        licence_terms = cls(
            id=id,
            name=name,
            commercial_use=commercial_use,
            share_alike=share_alike,
            attribution_required=attribution_required,
            redistribution=redistribution,
            attribution_text=attribution_text,
            terms_url=terms_url,
        )

        licence_terms.additional_properties = d
        return licence_terms

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
