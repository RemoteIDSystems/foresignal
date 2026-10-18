from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.coverage_json_type import CoverageJsonType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.coverage_json_coverages_item import CoverageJsonCoveragesItem
    from ..models.coverage_json_domain import CoverageJsonDomain
    from ..models.coverage_json_parameters import CoverageJsonParameters
    from ..models.coverage_json_ranges import CoverageJsonRanges


T = TypeVar("T", bound="CoverageJson")


@_attrs_define
class CoverageJson:
    """CoverageJSON document (application/prs.coverage+json): domain, parameters with units, and ranges with the nodata
    mask

        Attributes:
            type_ (CoverageJsonType):
            domain (CoverageJsonDomain | Unset): Domain axes and referencing
            parameters (CoverageJsonParameters | Unset):
            ranges (CoverageJsonRanges | Unset):
            coverages (list[CoverageJsonCoveragesItem] | Unset):
    """

    type_: CoverageJsonType
    domain: CoverageJsonDomain | Unset = UNSET
    parameters: CoverageJsonParameters | Unset = UNSET
    ranges: CoverageJsonRanges | Unset = UNSET
    coverages: list[CoverageJsonCoveragesItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        domain: dict[str, Any] | Unset = UNSET
        if not isinstance(self.domain, Unset):
            domain = self.domain.to_dict()

        parameters: dict[str, Any] | Unset = UNSET
        if not isinstance(self.parameters, Unset):
            parameters = self.parameters.to_dict()

        ranges: dict[str, Any] | Unset = UNSET
        if not isinstance(self.ranges, Unset):
            ranges = self.ranges.to_dict()

        coverages: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.coverages, Unset):
            coverages = []
            for coverages_item_data in self.coverages:
                coverages_item = coverages_item_data.to_dict()
                coverages.append(coverages_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if domain is not UNSET:
            field_dict["domain"] = domain
        if parameters is not UNSET:
            field_dict["parameters"] = parameters
        if ranges is not UNSET:
            field_dict["ranges"] = ranges
        if coverages is not UNSET:
            field_dict["coverages"] = coverages

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.coverage_json_coverages_item import CoverageJsonCoveragesItem
        from ..models.coverage_json_domain import CoverageJsonDomain
        from ..models.coverage_json_parameters import CoverageJsonParameters
        from ..models.coverage_json_ranges import CoverageJsonRanges

        d = dict(src_dict)
        type_ = CoverageJsonType(d.pop("type"))

        _domain = d.pop("domain", UNSET)
        domain: CoverageJsonDomain | Unset
        if isinstance(_domain, Unset):
            domain = UNSET
        else:
            domain = CoverageJsonDomain.from_dict(_domain)

        _parameters = d.pop("parameters", UNSET)
        parameters: CoverageJsonParameters | Unset
        if isinstance(_parameters, Unset):
            parameters = UNSET
        else:
            parameters = CoverageJsonParameters.from_dict(_parameters)

        _ranges = d.pop("ranges", UNSET)
        ranges: CoverageJsonRanges | Unset
        if isinstance(_ranges, Unset):
            ranges = UNSET
        else:
            ranges = CoverageJsonRanges.from_dict(_ranges)

        _coverages = d.pop("coverages", UNSET)
        coverages: list[CoverageJsonCoveragesItem] | Unset = UNSET
        if _coverages is not UNSET:
            coverages = []
            for coverages_item_data in _coverages:
                coverages_item = CoverageJsonCoveragesItem.from_dict(
                    coverages_item_data
                )

                coverages.append(coverages_item)

        coverage_json = cls(
            type_=type_,
            domain=domain,
            parameters=parameters,
            ranges=ranges,
            coverages=coverages,
        )

        coverage_json.additional_properties = d
        return coverage_json

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
