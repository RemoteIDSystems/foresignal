from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mapping_rejection import MappingRejection
    from ..models.mapping_warning import MappingWarning
    from ..models.normalized_record_preview import NormalizedRecordPreview


T = TypeVar("T", bound="MappingTestResultSamplesItem")


@_attrs_define
class MappingTestResultSamplesItem:
    """
    Attributes:
        records (list[NormalizedRecordPreview]):
        rejections (list[MappingRejection]):
        warnings (list[MappingWarning]):
        received_at (datetime.datetime):
        skipped (int | Unset): Items dropped by `filter`
    """

    records: list[NormalizedRecordPreview]
    rejections: list[MappingRejection]
    warnings: list[MappingWarning]
    received_at: datetime.datetime
    skipped: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        records = []
        for records_item_data in self.records:
            records_item = records_item_data.to_dict()
            records.append(records_item)

        rejections = []
        for rejections_item_data in self.rejections:
            rejections_item = rejections_item_data.to_dict()
            rejections.append(rejections_item)

        warnings = []
        for warnings_item_data in self.warnings:
            warnings_item = warnings_item_data.to_dict()
            warnings.append(warnings_item)

        received_at = self.received_at.isoformat()

        skipped = self.skipped

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "records": records,
                "rejections": rejections,
                "warnings": warnings,
                "receivedAt": received_at,
            }
        )
        if skipped is not UNSET:
            field_dict["skipped"] = skipped

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mapping_rejection import MappingRejection
        from ..models.mapping_warning import MappingWarning
        from ..models.normalized_record_preview import NormalizedRecordPreview

        d = dict(src_dict)
        records = []
        _records = d.pop("records")
        for records_item_data in _records:
            records_item = NormalizedRecordPreview.from_dict(records_item_data)

            records.append(records_item)

        rejections = []
        _rejections = d.pop("rejections")
        for rejections_item_data in _rejections:
            rejections_item = MappingRejection.from_dict(rejections_item_data)

            rejections.append(rejections_item)

        warnings = []
        _warnings = d.pop("warnings")
        for warnings_item_data in _warnings:
            warnings_item = MappingWarning.from_dict(warnings_item_data)

            warnings.append(warnings_item)

        received_at = datetime.datetime.fromisoformat(d.pop("receivedAt"))

        skipped = d.pop("skipped", UNSET)

        mapping_test_result_samples_item = cls(
            records=records,
            rejections=rejections,
            warnings=warnings,
            received_at=received_at,
            skipped=skipped,
        )

        mapping_test_result_samples_item.additional_properties = d
        return mapping_test_result_samples_item

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
