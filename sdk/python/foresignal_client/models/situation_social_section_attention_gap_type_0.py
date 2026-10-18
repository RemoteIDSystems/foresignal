from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SituationSocialSectionAttentionGapType0")


@_attrs_define
class SituationSocialSectionAttentionGapType0:
    """Attention gap

    Attributes:
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        expected (float):
        observed (float):
        platforms (list[str] | Unset):
    """

    finding_id: str
    expected: float
    observed: float
    platforms: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        finding_id = self.finding_id

        expected = self.expected

        observed = self.observed

        platforms: list[str] | Unset = UNSET
        if not isinstance(self.platforms, Unset):
            platforms = self.platforms

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "findingId": finding_id,
                "expected": expected,
                "observed": observed,
            }
        )
        if platforms is not UNSET:
            field_dict["platforms"] = platforms

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        finding_id = d.pop("findingId")

        expected = d.pop("expected")

        observed = d.pop("observed")

        platforms = cast(list[str], d.pop("platforms", UNSET))

        situation_social_section_attention_gap_type_0 = cls(
            finding_id=finding_id,
            expected=expected,
            observed=observed,
            platforms=platforms,
        )

        situation_social_section_attention_gap_type_0.additional_properties = d
        return situation_social_section_attention_gap_type_0

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
