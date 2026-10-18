from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.export_licence_summary_excluded_item import (
        ExportLicenceSummaryExcludedItem,
    )
    from ..models.export_licence_summary_licences_item import (
        ExportLicenceSummaryLicencesItem,
    )


T = TypeVar("T", bound="ExportLicenceSummary")


@_attrs_define
class ExportLicenceSummary:
    """Licence sidecar summary of an export, snapshot, pack or archive file (111)

    Attributes:
        licences (list[ExportLicenceSummaryLicencesItem]):
        attributions (list[Attribution]):
        personal_use_only (bool):
        personal_use_sources (list[str] | Unset): Sources included only because personalUse=true
        excluded (list[ExportLicenceSummaryExcludedItem] | Unset):
    """

    licences: list[ExportLicenceSummaryLicencesItem]
    attributions: list[Attribution]
    personal_use_only: bool
    personal_use_sources: list[str] | Unset = UNSET
    excluded: list[ExportLicenceSummaryExcludedItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        licences = []
        for licences_item_data in self.licences:
            licences_item = licences_item_data.to_dict()
            licences.append(licences_item)

        attributions = []
        for attributions_item_data in self.attributions:
            attributions_item = attributions_item_data.to_dict()
            attributions.append(attributions_item)

        personal_use_only = self.personal_use_only

        personal_use_sources: list[str] | Unset = UNSET
        if not isinstance(self.personal_use_sources, Unset):
            personal_use_sources = self.personal_use_sources

        excluded: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.excluded, Unset):
            excluded = []
            for excluded_item_data in self.excluded:
                excluded_item = excluded_item_data.to_dict()
                excluded.append(excluded_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "licences": licences,
                "attributions": attributions,
                "personalUseOnly": personal_use_only,
            }
        )
        if personal_use_sources is not UNSET:
            field_dict["personalUseSources"] = personal_use_sources
        if excluded is not UNSET:
            field_dict["excluded"] = excluded

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.export_licence_summary_excluded_item import (
            ExportLicenceSummaryExcludedItem,
        )
        from ..models.export_licence_summary_licences_item import (
            ExportLicenceSummaryLicencesItem,
        )

        d = dict(src_dict)
        licences = []
        _licences = d.pop("licences")
        for licences_item_data in _licences:
            licences_item = ExportLicenceSummaryLicencesItem.from_dict(
                licences_item_data
            )

            licences.append(licences_item)

        attributions = []
        _attributions = d.pop("attributions")
        for attributions_item_data in _attributions:
            attributions_item = Attribution.from_dict(attributions_item_data)

            attributions.append(attributions_item)

        personal_use_only = d.pop("personalUseOnly")

        personal_use_sources = cast(list[str], d.pop("personalUseSources", UNSET))

        _excluded = d.pop("excluded", UNSET)
        excluded: list[ExportLicenceSummaryExcludedItem] | Unset = UNSET
        if _excluded is not UNSET:
            excluded = []
            for excluded_item_data in _excluded:
                excluded_item = ExportLicenceSummaryExcludedItem.from_dict(
                    excluded_item_data
                )

                excluded.append(excluded_item)

        export_licence_summary = cls(
            licences=licences,
            attributions=attributions,
            personal_use_only=personal_use_only,
            personal_use_sources=personal_use_sources,
            excluded=excluded,
        )

        export_licence_summary.additional_properties = d
        return export_licence_summary

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
