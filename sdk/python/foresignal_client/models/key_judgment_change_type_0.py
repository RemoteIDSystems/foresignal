from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="KeyJudgmentChangeType0")


@_attrs_define
class KeyJudgmentChangeType0:
    """Change against the previous assessment

    Attributes:
        explanation (str):
        previous_probability (float | None | Unset):
        previous_text (None | str | Unset):
    """

    explanation: str
    previous_probability: float | None | Unset = UNSET
    previous_text: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        explanation = self.explanation

        previous_probability: float | None | Unset
        if isinstance(self.previous_probability, Unset):
            previous_probability = UNSET
        else:
            previous_probability = self.previous_probability

        previous_text: None | str | Unset
        if isinstance(self.previous_text, Unset):
            previous_text = UNSET
        else:
            previous_text = self.previous_text

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "explanation": explanation,
            }
        )
        if previous_probability is not UNSET:
            field_dict["previousProbability"] = previous_probability
        if previous_text is not UNSET:
            field_dict["previousText"] = previous_text

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        explanation = d.pop("explanation")

        def _parse_previous_probability(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        previous_probability = _parse_previous_probability(
            d.pop("previousProbability", UNSET)
        )

        def _parse_previous_text(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        previous_text = _parse_previous_text(d.pop("previousText", UNSET))

        key_judgment_change_type_0 = cls(
            explanation=explanation,
            previous_probability=previous_probability,
            previous_text=previous_text,
        )

        key_judgment_change_type_0.additional_properties = d
        return key_judgment_change_type_0

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
