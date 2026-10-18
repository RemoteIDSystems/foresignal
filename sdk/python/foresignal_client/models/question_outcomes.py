from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="QuestionOutcomes")


@_attrs_define
class QuestionOutcomes:
    """Outcome space: labels for multiple/ordered questions, unit and bounds for numeric

    Attributes:
        labels (list[str] | Unset): Multiple (unordered) or ordered labels, in order
        unit (None | str | Unset):
        lower (float | None | Unset):
        upper (float | None | Unset):
    """

    labels: list[str] | Unset = UNSET
    unit: None | str | Unset = UNSET
    lower: float | None | Unset = UNSET
    upper: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        labels: list[str] | Unset = UNSET
        if not isinstance(self.labels, Unset):
            labels = self.labels

        unit: None | str | Unset
        if isinstance(self.unit, Unset):
            unit = UNSET
        else:
            unit = self.unit

        lower: float | None | Unset
        if isinstance(self.lower, Unset):
            lower = UNSET
        else:
            lower = self.lower

        upper: float | None | Unset
        if isinstance(self.upper, Unset):
            upper = UNSET
        else:
            upper = self.upper

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if labels is not UNSET:
            field_dict["labels"] = labels
        if unit is not UNSET:
            field_dict["unit"] = unit
        if lower is not UNSET:
            field_dict["lower"] = lower
        if upper is not UNSET:
            field_dict["upper"] = upper

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        labels = cast(list[str], d.pop("labels", UNSET))

        def _parse_unit(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        unit = _parse_unit(d.pop("unit", UNSET))

        def _parse_lower(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lower = _parse_lower(d.pop("lower", UNSET))

        def _parse_upper(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        upper = _parse_upper(d.pop("upper", UNSET))

        question_outcomes = cls(
            labels=labels,
            unit=unit,
            lower=lower,
            upper=upper,
        )

        question_outcomes.additional_properties = d
        return question_outcomes

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
