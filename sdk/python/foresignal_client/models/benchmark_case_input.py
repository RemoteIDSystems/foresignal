from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.benchmark_case_input_expectations import (
        BenchmarkCaseInputExpectations,
    )


T = TypeVar("T", bound="BenchmarkCaseInput")


@_attrs_define
class BenchmarkCaseInput:
    """A new operator or agent historical case

    Attributes:
        name (str):
        window_from (datetime.datetime):
        window_to (datetime.datetime):
        expectations (BenchmarkCaseInputExpectations): Expected outcomes for a historical case
        description (None | str | Unset):
        region_codes (list[str] | Unset): ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
    """

    name: str
    window_from: datetime.datetime
    window_to: datetime.datetime
    expectations: BenchmarkCaseInputExpectations
    description: None | str | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        window_from = self.window_from.isoformat()

        window_to = self.window_to.isoformat()

        expectations = self.expectations.to_dict()

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "windowFrom": window_from,
                "windowTo": window_to,
                "expectations": expectations,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.benchmark_case_input_expectations import (
            BenchmarkCaseInputExpectations,
        )

        d = dict(src_dict)
        name = d.pop("name")

        window_from = datetime.datetime.fromisoformat(d.pop("windowFrom"))

        window_to = datetime.datetime.fromisoformat(d.pop("windowTo"))

        expectations = BenchmarkCaseInputExpectations.from_dict(d.pop("expectations"))

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        benchmark_case_input = cls(
            name=name,
            window_from=window_from,
            window_to=window_to,
            expectations=expectations,
            description=description,
            region_codes=region_codes,
        )

        benchmark_case_input.additional_properties = d
        return benchmark_case_input

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
