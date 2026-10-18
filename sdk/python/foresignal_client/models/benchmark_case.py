from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.benchmark_case_source import BenchmarkCaseSource
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.benchmark_case_expectations import BenchmarkCaseExpectations


T = TypeVar("T", bound="BenchmarkCase")


@_attrs_define
class BenchmarkCase:
    """A historical benchmark case replayed with `asOf` stepping from the window start

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        name (str):
        window_from (datetime.datetime):
        window_to (datetime.datetime):
        region_codes (list[str]): ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
        expectations (BenchmarkCaseExpectations): Expected outcomes for a historical case
        source (BenchmarkCaseSource): `auto` = sampled null region-week
        created_at (datetime.datetime):
        description (None | str | Unset):
        created_by (None | str | Unset):
    """

    id: str
    name: str
    window_from: datetime.datetime
    window_to: datetime.datetime
    region_codes: list[str]
    expectations: BenchmarkCaseExpectations
    source: BenchmarkCaseSource
    created_at: datetime.datetime
    description: None | str | Unset = UNSET
    created_by: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        name = self.name

        window_from = self.window_from.isoformat()

        window_to = self.window_to.isoformat()

        region_codes = self.region_codes

        expectations = self.expectations.to_dict()

        source = self.source.value

        created_at = self.created_at.isoformat()

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        created_by: None | str | Unset
        if isinstance(self.created_by, Unset):
            created_by = UNSET
        else:
            created_by = self.created_by

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "windowFrom": window_from,
                "windowTo": window_to,
                "regionCodes": region_codes,
                "expectations": expectations,
                "source": source,
                "createdAt": created_at,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if created_by is not UNSET:
            field_dict["createdBy"] = created_by

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.benchmark_case_expectations import BenchmarkCaseExpectations

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        window_from = datetime.datetime.fromisoformat(d.pop("windowFrom"))

        window_to = datetime.datetime.fromisoformat(d.pop("windowTo"))

        region_codes = cast(list[str], d.pop("regionCodes"))

        expectations = BenchmarkCaseExpectations.from_dict(d.pop("expectations"))

        source = BenchmarkCaseSource(d.pop("source"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        def _parse_created_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        created_by = _parse_created_by(d.pop("createdBy", UNSET))

        benchmark_case = cls(
            id=id,
            name=name,
            window_from=window_from,
            window_to=window_to,
            region_codes=region_codes,
            expectations=expectations,
            source=source,
            created_at=created_at,
            description=description,
            created_by=created_by,
        )

        benchmark_case.additional_properties = d
        return benchmark_case

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
