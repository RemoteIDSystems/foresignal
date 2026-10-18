from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="Coverage")


@_attrs_define
class Coverage:
    """Receiver coverage reported by a fetch (fetch_log.coverage, source_health.last_coverage)

    Attributes:
        receivers (int | None | Unset):
        expected (int | None | Unset):
        actual (int | None | Unset):
        region (list[float] | None | Unset): Covered region [minLon, minLat, maxLon, maxLat]
    """

    receivers: int | None | Unset = UNSET
    expected: int | None | Unset = UNSET
    actual: int | None | Unset = UNSET
    region: list[float] | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        receivers: int | None | Unset
        if isinstance(self.receivers, Unset):
            receivers = UNSET
        else:
            receivers = self.receivers

        expected: int | None | Unset
        if isinstance(self.expected, Unset):
            expected = UNSET
        else:
            expected = self.expected

        actual: int | None | Unset
        if isinstance(self.actual, Unset):
            actual = UNSET
        else:
            actual = self.actual

        region: list[float] | None | Unset
        if isinstance(self.region, Unset):
            region = UNSET
        elif isinstance(self.region, list):
            region = self.region

        else:
            region = self.region

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if receivers is not UNSET:
            field_dict["receivers"] = receivers
        if expected is not UNSET:
            field_dict["expected"] = expected
        if actual is not UNSET:
            field_dict["actual"] = actual
        if region is not UNSET:
            field_dict["region"] = region

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_receivers(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        receivers = _parse_receivers(d.pop("receivers", UNSET))

        def _parse_expected(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        expected = _parse_expected(d.pop("expected", UNSET))

        def _parse_actual(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        actual = _parse_actual(d.pop("actual", UNSET))

        def _parse_region(data: object) -> list[float] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                region_type_0 = cast(list[float], data)

                return region_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[float] | None | Unset, data)

        region = _parse_region(d.pop("region", UNSET))

        coverage = cls(
            receivers=receivers,
            expected=expected,
            actual=actual,
            region=region,
        )

        coverage.additional_properties = d
        return coverage

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
