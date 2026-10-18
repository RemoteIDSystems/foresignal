from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ForecastCitationsItem")


@_attrs_define
class ForecastCitationsItem:
    """A citation supporting a forecast

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        quote (None | str | Unset):
        claim (None | str | Unset):
    """

    ref: str
    quote: None | str | Unset = UNSET
    claim: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        quote: None | str | Unset
        if isinstance(self.quote, Unset):
            quote = UNSET
        else:
            quote = self.quote

        claim: None | str | Unset
        if isinstance(self.claim, Unset):
            claim = UNSET
        else:
            claim = self.claim

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
            }
        )
        if quote is not UNSET:
            field_dict["quote"] = quote
        if claim is not UNSET:
            field_dict["claim"] = claim

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ref = d.pop("ref")

        def _parse_quote(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        quote = _parse_quote(d.pop("quote", UNSET))

        def _parse_claim(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        claim = _parse_claim(d.pop("claim", UNSET))

        forecast_citations_item = cls(
            ref=ref,
            quote=quote,
            claim=claim,
        )

        forecast_citations_item.additional_properties = d
        return forecast_citations_item

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
