from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.licence_terms import LicenceTerms


T = TypeVar("T", bound="ExportLicenceSummaryLicencesItem")


@_attrs_define
class ExportLicenceSummaryLicencesItem:
    """Rows per licence

    Attributes:
        terms (LicenceTerms): Structured licence terms resolved for a source or a record version (109)
        sources (list[str]):
        rows (int):
    """

    terms: LicenceTerms
    sources: list[str]
    rows: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        terms = self.terms.to_dict()

        sources = self.sources

        rows = self.rows

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "terms": terms,
                "sources": sources,
                "rows": rows,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.licence_terms import LicenceTerms

        d = dict(src_dict)
        terms = LicenceTerms.from_dict(d.pop("terms"))

        sources = cast(list[str], d.pop("sources"))

        rows = d.pop("rows")

        export_licence_summary_licences_item = cls(
            terms=terms,
            sources=sources,
            rows=rows,
        )

        export_licence_summary_licences_item.additional_properties = d
        return export_licence_summary_licences_item

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
