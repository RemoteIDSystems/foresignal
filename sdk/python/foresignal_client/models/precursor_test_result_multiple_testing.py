from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PrecursorTestResultMultipleTesting")


@_attrs_define
class PrecursorTestResultMultipleTesting:
    """Tests run on this instance in the last 30 days

    Attributes:
        tests_last_30_d (int | Unset):
        bonferroni_p_value (float | None | Unset):
    """

    tests_last_30_d: int | Unset = UNSET
    bonferroni_p_value: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        tests_last_30_d = self.tests_last_30_d

        bonferroni_p_value: float | None | Unset
        if isinstance(self.bonferroni_p_value, Unset):
            bonferroni_p_value = UNSET
        else:
            bonferroni_p_value = self.bonferroni_p_value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if tests_last_30_d is not UNSET:
            field_dict["testsLast30d"] = tests_last_30_d
        if bonferroni_p_value is not UNSET:
            field_dict["bonferroniPValue"] = bonferroni_p_value

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        tests_last_30_d = d.pop("testsLast30d", UNSET)

        def _parse_bonferroni_p_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        bonferroni_p_value = _parse_bonferroni_p_value(d.pop("bonferroniPValue", UNSET))

        precursor_test_result_multiple_testing = cls(
            tests_last_30_d=tests_last_30_d,
            bonferroni_p_value=bonferroni_p_value,
        )

        precursor_test_result_multiple_testing.additional_properties = d
        return precursor_test_result_multiple_testing

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
