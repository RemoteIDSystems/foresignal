from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="HypothesisSetDraftRequest")


@_attrs_define
class HypothesisSetDraftRequest:
    """Plain-language request for an LLM-drafted hypothesis set

    Attributes:
        text (str):  Example: Watch whether the Red Sea situation spreads to Hormuz.
        situation_id (None | str | Unset):
        region_codes (list[str] | Unset): ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
    """

    text: str
    situation_id: None | str | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        text = self.text

        situation_id: None | str | Unset
        if isinstance(self.situation_id, Unset):
            situation_id = UNSET
        else:
            situation_id = self.situation_id

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "text": text,
            }
        )
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        text = d.pop("text")

        def _parse_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        situation_id = _parse_situation_id(d.pop("situationId", UNSET))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        hypothesis_set_draft_request = cls(
            text=text,
            situation_id=situation_id,
            region_codes=region_codes,
        )

        hypothesis_set_draft_request.additional_properties = d
        return hypothesis_set_draft_request

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
