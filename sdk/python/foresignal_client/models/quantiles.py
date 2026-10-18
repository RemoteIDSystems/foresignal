from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="Quantiles")


@_attrs_define
class Quantiles:
    """
    Attributes:
        p05 (float):
        p25 (float):
        p50 (float):
        p75 (float):
        p95 (float):
        p10 (float | Unset):
        p90 (float | Unset):
    """

    p05: float
    p25: float
    p50: float
    p75: float
    p95: float
    p10: float | Unset = UNSET
    p90: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        p05 = self.p05

        p25 = self.p25

        p50 = self.p50

        p75 = self.p75

        p95 = self.p95

        p10 = self.p10

        p90 = self.p90

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "p05": p05,
                "p25": p25,
                "p50": p50,
                "p75": p75,
                "p95": p95,
            }
        )
        if p10 is not UNSET:
            field_dict["p10"] = p10
        if p90 is not UNSET:
            field_dict["p90"] = p90

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        p05 = d.pop("p05")

        p25 = d.pop("p25")

        p50 = d.pop("p50")

        p75 = d.pop("p75")

        p95 = d.pop("p95")

        p10 = d.pop("p10", UNSET)

        p90 = d.pop("p90", UNSET)

        quantiles = cls(
            p05=p05,
            p25=p25,
            p50=p50,
            p75=p75,
            p95=p95,
            p10=p10,
            p90=p90,
        )

        quantiles.additional_properties = d
        return quantiles

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
