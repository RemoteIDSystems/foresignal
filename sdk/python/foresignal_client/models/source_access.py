from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.source_access_fragility import SourceAccessFragility
from ..models.source_access_route import SourceAccessRoute
from ..types import UNSET, Unset

T = TypeVar("T", bound="SourceAccess")


@_attrs_define
class SourceAccess:
    """How a source reaches its data and what enabling it involves

    Attributes:
        route (SourceAccessRoute):
        terms_summary (None | str | Unset):
        terms_url (None | str | Unset):
        fragility (SourceAccessFragility | Unset):
    """

    route: SourceAccessRoute
    terms_summary: None | str | Unset = UNSET
    terms_url: None | str | Unset = UNSET
    fragility: SourceAccessFragility | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        route = self.route.value

        terms_summary: None | str | Unset
        if isinstance(self.terms_summary, Unset):
            terms_summary = UNSET
        else:
            terms_summary = self.terms_summary

        terms_url: None | str | Unset
        if isinstance(self.terms_url, Unset):
            terms_url = UNSET
        else:
            terms_url = self.terms_url

        fragility: str | Unset = UNSET
        if not isinstance(self.fragility, Unset):
            fragility = self.fragility.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "route": route,
            }
        )
        if terms_summary is not UNSET:
            field_dict["termsSummary"] = terms_summary
        if terms_url is not UNSET:
            field_dict["termsUrl"] = terms_url
        if fragility is not UNSET:
            field_dict["fragility"] = fragility

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        route = SourceAccessRoute(d.pop("route"))

        def _parse_terms_summary(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        terms_summary = _parse_terms_summary(d.pop("termsSummary", UNSET))

        def _parse_terms_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        terms_url = _parse_terms_url(d.pop("termsUrl", UNSET))

        _fragility = d.pop("fragility", UNSET)
        fragility: SourceAccessFragility | Unset
        if isinstance(_fragility, Unset):
            fragility = UNSET
        else:
            fragility = SourceAccessFragility(_fragility)

        source_access = cls(
            route=route,
            terms_summary=terms_summary,
            terms_url=terms_url,
            fragility=fragility,
        )

        source_access.additional_properties = d
        return source_access

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
