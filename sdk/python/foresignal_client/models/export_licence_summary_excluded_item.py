from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.export_licence_summary_excluded_item_reason import (
    ExportLicenceSummaryExcludedItemReason,
)

T = TypeVar("T", bound="ExportLicenceSummaryExcludedItem")


@_attrs_define
class ExportLicenceSummaryExcludedItem:
    """A source excluded by the egress guard

    Attributes:
        source_id (str):
        reason (ExportLicenceSummaryExcludedItemReason):
    """

    source_id: str
    reason: ExportLicenceSummaryExcludedItemReason
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        reason = self.reason.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "reason": reason,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        reason = ExportLicenceSummaryExcludedItemReason(d.pop("reason"))

        export_licence_summary_excluded_item = cls(
            source_id=source_id,
            reason=reason,
        )

        export_licence_summary_excluded_item.additional_properties = d
        return export_licence_summary_excluded_item

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
