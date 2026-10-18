from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.finding_severity import FindingSeverity
from ..types import UNSET, Unset

T = TypeVar("T", bound="BenchmarkCaseInputExpectationsFindingsItem")


@_attrs_define
class BenchmarkCaseInputExpectationsFindingsItem:
    """Expected finding (or its absence)

    Attributes:
        finding_type (str):  Example: change-point.
        present (bool):
        region_codes (list[str] | Unset): ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
        series_ref (None | str | Unset):
        from_ (datetime.datetime | None | Unset):
        to (datetime.datetime | None | Unset):
        min_severity (FindingSeverity | None | Unset):
    """

    finding_type: str
    present: bool
    region_codes: list[str] | Unset = UNSET
    series_ref: None | str | Unset = UNSET
    from_: datetime.datetime | None | Unset = UNSET
    to: datetime.datetime | None | Unset = UNSET
    min_severity: FindingSeverity | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        finding_type = self.finding_type

        present = self.present

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        series_ref: None | str | Unset
        if isinstance(self.series_ref, Unset):
            series_ref = UNSET
        else:
            series_ref = self.series_ref

        from_: None | str | Unset
        if isinstance(self.from_, Unset):
            from_ = UNSET
        elif isinstance(self.from_, datetime.datetime):
            from_ = self.from_.isoformat()
        else:
            from_ = self.from_

        to: None | str | Unset
        if isinstance(self.to, Unset):
            to = UNSET
        elif isinstance(self.to, datetime.datetime):
            to = self.to.isoformat()
        else:
            to = self.to

        min_severity: None | str | Unset
        if isinstance(self.min_severity, Unset):
            min_severity = UNSET
        elif isinstance(self.min_severity, FindingSeverity):
            min_severity = self.min_severity.value
        else:
            min_severity = self.min_severity

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "findingType": finding_type,
                "present": present,
            }
        )
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if series_ref is not UNSET:
            field_dict["seriesRef"] = series_ref
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if min_severity is not UNSET:
            field_dict["minSeverity"] = min_severity

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        finding_type = d.pop("findingType")

        present = d.pop("present")

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        def _parse_series_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        series_ref = _parse_series_ref(d.pop("seriesRef", UNSET))

        def _parse_from_(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                from_type_0 = datetime.datetime.fromisoformat(data)

                return from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        from_ = _parse_from_(d.pop("from", UNSET))

        def _parse_to(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                to_type_0 = datetime.datetime.fromisoformat(data)

                return to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        to = _parse_to(d.pop("to", UNSET))

        def _parse_min_severity(data: object) -> FindingSeverity | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                min_severity_type_0 = FindingSeverity(data)

                return min_severity_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(FindingSeverity | None | Unset, data)

        min_severity = _parse_min_severity(d.pop("minSeverity", UNSET))

        benchmark_case_input_expectations_findings_item = cls(
            finding_type=finding_type,
            present=present,
            region_codes=region_codes,
            series_ref=series_ref,
            from_=from_,
            to=to,
            min_severity=min_severity,
        )

        benchmark_case_input_expectations_findings_item.additional_properties = d
        return benchmark_case_input_expectations_findings_item

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
