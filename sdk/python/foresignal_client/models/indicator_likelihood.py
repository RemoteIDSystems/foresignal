from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.indicator_likelihood_class import IndicatorLikelihoodClass
from ..models.indicator_likelihood_direction import IndicatorLikelihoodDirection
from ..types import UNSET, Unset

T = TypeVar("T", bound="IndicatorLikelihood")


@_attrs_define
class IndicatorLikelihood:
    """Likelihood ratio of an indicator for one hypothesis

    Attributes:
        class_ (IndicatorLikelihoodClass): Diagnosticity class: high 5.0/0.2, medium 2.5/0.4, low 1.4/0.7, neutral 1.0
        direction (IndicatorLikelihoodDirection):
        value (float | None | Unset): Explicit LR overriding the class value; requires a rationale
        rationale (None | str | Unset):
        effective (float | Unset): LR the updater applies
    """

    class_: IndicatorLikelihoodClass
    direction: IndicatorLikelihoodDirection
    value: float | None | Unset = UNSET
    rationale: None | str | Unset = UNSET
    effective: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        class_ = self.class_.value

        direction = self.direction.value

        value: float | None | Unset
        if isinstance(self.value, Unset):
            value = UNSET
        else:
            value = self.value

        rationale: None | str | Unset
        if isinstance(self.rationale, Unset):
            rationale = UNSET
        else:
            rationale = self.rationale

        effective = self.effective

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "class": class_,
                "direction": direction,
            }
        )
        if value is not UNSET:
            field_dict["value"] = value
        if rationale is not UNSET:
            field_dict["rationale"] = rationale
        if effective is not UNSET:
            field_dict["effective"] = effective

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        class_ = IndicatorLikelihoodClass(d.pop("class"))

        direction = IndicatorLikelihoodDirection(d.pop("direction"))

        def _parse_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        value = _parse_value(d.pop("value", UNSET))

        def _parse_rationale(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        rationale = _parse_rationale(d.pop("rationale", UNSET))

        effective = d.pop("effective", UNSET)

        indicator_likelihood = cls(
            class_=class_,
            direction=direction,
            value=value,
            rationale=rationale,
            effective=effective,
        )

        indicator_likelihood.additional_properties = d
        return indicator_likelihood

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
