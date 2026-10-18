from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="AnalyticFindingEvidence")


@_attrs_define
class AnalyticFindingEvidence:
    """Evidence: the series and pinned members the finding rests on

    Attributes:
        series_refs (list[str]):
        members (list[str]): Pinned refs (records with `@seq`, intel events, situations, findings)
        values_withheld (bool | Unset): Raw values of `LOCAL_ONLY` series withheld; standardised statistics remain
    """

    series_refs: list[str]
    members: list[str]
    values_withheld: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        series_refs = self.series_refs

        members = self.members

        values_withheld = self.values_withheld

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "seriesRefs": series_refs,
                "members": members,
            }
        )
        if values_withheld is not UNSET:
            field_dict["valuesWithheld"] = values_withheld

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        series_refs = cast(list[str], d.pop("seriesRefs"))

        members = cast(list[str], d.pop("members"))

        values_withheld = d.pop("valuesWithheld", UNSET)

        analytic_finding_evidence = cls(
            series_refs=series_refs,
            members=members,
            values_withheld=values_withheld,
        )

        analytic_finding_evidence.additional_properties = d
        return analytic_finding_evidence

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
