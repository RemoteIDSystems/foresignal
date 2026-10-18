from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CycleScope")


@_attrs_define
class CycleScope:
    """What a cycle covers

    Attributes:
        region_codes (list[str] | Unset): ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
        themes (list[str] | Unset):
        situation_ids (list[str] | Unset):
        hypothesis_set_ids (list[str] | Literal['cadence-match'] | Unset): Set ids, or `cadence-match` for every set
            whose cadence matches
        source_ids (list[str] | Unset):
        categories (list[str] | Unset):
    """

    region_codes: list[str] | Unset = UNSET
    themes: list[str] | Unset = UNSET
    situation_ids: list[str] | Unset = UNSET
    hypothesis_set_ids: list[str] | Literal["cadence-match"] | Unset = UNSET
    source_ids: list[str] | Unset = UNSET
    categories: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        themes: list[str] | Unset = UNSET
        if not isinstance(self.themes, Unset):
            themes = self.themes

        situation_ids: list[str] | Unset = UNSET
        if not isinstance(self.situation_ids, Unset):
            situation_ids = self.situation_ids

        hypothesis_set_ids: list[str] | Literal["cadence-match"] | Unset
        if isinstance(self.hypothesis_set_ids, Unset):
            hypothesis_set_ids = UNSET
        elif isinstance(self.hypothesis_set_ids, list):
            hypothesis_set_ids = self.hypothesis_set_ids

        else:
            hypothesis_set_ids = self.hypothesis_set_ids

        source_ids: list[str] | Unset = UNSET
        if not isinstance(self.source_ids, Unset):
            source_ids = self.source_ids

        categories: list[str] | Unset = UNSET
        if not isinstance(self.categories, Unset):
            categories = self.categories

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if themes is not UNSET:
            field_dict["themes"] = themes
        if situation_ids is not UNSET:
            field_dict["situationIds"] = situation_ids
        if hypothesis_set_ids is not UNSET:
            field_dict["hypothesisSetIds"] = hypothesis_set_ids
        if source_ids is not UNSET:
            field_dict["sourceIds"] = source_ids
        if categories is not UNSET:
            field_dict["categories"] = categories

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        themes = cast(list[str], d.pop("themes", UNSET))

        situation_ids = cast(list[str], d.pop("situationIds", UNSET))

        def _parse_hypothesis_set_ids(
            data: object,
        ) -> list[str] | Literal["cadence-match"] | Unset:
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                hypothesis_set_ids_type_0 = cast(list[str], data)

                return hypothesis_set_ids_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            hypothesis_set_ids_type_1 = cast(Literal["cadence-match"], data)
            if hypothesis_set_ids_type_1 != "cadence-match":
                raise ValueError(
                    f"hypothesisSetIds_type_1 must match const 'cadence-match', got '{hypothesis_set_ids_type_1}'"
                )
            return hypothesis_set_ids_type_1

        hypothesis_set_ids = _parse_hypothesis_set_ids(d.pop("hypothesisSetIds", UNSET))

        source_ids = cast(list[str], d.pop("sourceIds", UNSET))

        categories = cast(list[str], d.pop("categories", UNSET))

        cycle_scope = cls(
            region_codes=region_codes,
            themes=themes,
            situation_ids=situation_ids,
            hypothesis_set_ids=hypothesis_set_ids,
            source_ids=source_ids,
            categories=categories,
        )

        cycle_scope.additional_properties = d
        return cycle_scope

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
