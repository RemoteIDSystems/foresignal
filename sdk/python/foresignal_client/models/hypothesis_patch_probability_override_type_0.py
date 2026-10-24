from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="HypothesisPatchProbabilityOverrideType0")


@_attrs_define
class HypothesisPatchProbabilityOverrideType0:
    """Operator override (cause `operator`)

    Attributes:
        probability (float):
        rationale (str):
        exceed_cap (bool | Unset): Allow a speculative link above its cap (logged and labeled) Default: False.
    """

    probability: float
    rationale: str
    exceed_cap: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        probability = self.probability

        rationale = self.rationale

        exceed_cap = self.exceed_cap

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "probability": probability,
                "rationale": rationale,
            }
        )
        if exceed_cap is not UNSET:
            field_dict["exceedCap"] = exceed_cap

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        probability = d.pop("probability")

        rationale = d.pop("rationale")

        exceed_cap = d.pop("exceedCap", UNSET)

        hypothesis_patch_probability_override_type_0 = cls(
            probability=probability,
            rationale=rationale,
            exceed_cap=exceed_cap,
        )

        hypothesis_patch_probability_override_type_0.additional_properties = d
        return hypothesis_patch_probability_override_type_0

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
