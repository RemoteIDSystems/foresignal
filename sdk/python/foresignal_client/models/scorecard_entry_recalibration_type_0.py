from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ScorecardEntryRecalibrationType0")


@_attrs_define
class ScorecardEntryRecalibrationType0:
    """Platt recalibration p' = σ(a · logit p + b)

    Attributes:
        a (float):
        b (float):
        n (int):
        fitted_at (datetime.datetime | Unset):
    """

    a: float
    b: float
    n: int
    fitted_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        a = self.a

        b = self.b

        n = self.n

        fitted_at: str | Unset = UNSET
        if not isinstance(self.fitted_at, Unset):
            fitted_at = self.fitted_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "a": a,
                "b": b,
                "n": n,
            }
        )
        if fitted_at is not UNSET:
            field_dict["fittedAt"] = fitted_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        a = d.pop("a")

        b = d.pop("b")

        n = d.pop("n")

        _fitted_at = d.pop("fittedAt", UNSET)
        fitted_at: datetime.datetime | Unset
        if isinstance(_fitted_at, Unset):
            fitted_at = UNSET
        else:
            fitted_at = datetime.datetime.fromisoformat(_fitted_at)

        scorecard_entry_recalibration_type_0 = cls(
            a=a,
            b=b,
            n=n,
            fitted_at=fitted_at,
        )

        scorecard_entry_recalibration_type_0.additional_properties = d
        return scorecard_entry_recalibration_type_0

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
