from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="EnsembleRefitResultPlattItem")


@_attrs_define
class EnsembleRefitResultPlattItem:
    """Platt fit per forecaster and category (≥ 50 resolved)

    Attributes:
        forecaster_id (str):
        category (str):
        a (float):
        b (float):
        n (int):
    """

    forecaster_id: str
    category: str
    a: float
    b: float
    n: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        forecaster_id = self.forecaster_id

        category = self.category

        a = self.a

        b = self.b

        n = self.n

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "forecasterId": forecaster_id,
                "category": category,
                "a": a,
                "b": b,
                "n": n,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        forecaster_id = d.pop("forecasterId")

        category = d.pop("category")

        a = d.pop("a")

        b = d.pop("b")

        n = d.pop("n")

        ensemble_refit_result_platt_item = cls(
            forecaster_id=forecaster_id,
            category=category,
            a=a,
            b=b,
            n=n,
        )

        ensemble_refit_result_platt_item.additional_properties = d
        return ensemble_refit_result_platt_item

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
