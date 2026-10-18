from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_item_rejection import EdpItemRejection
    from ..models.edp_validation_report_manifest_errors_item import (
        EdpValidationReportManifestErrorsItem,
    )


T = TypeVar("T", bound="EdpValidationReport")


@_attrs_define
class EdpValidationReport:
    """Dry-run validation result

    Attributes:
        manifest_valid (bool | None | Unset):
        manifest_errors (list[EdpValidationReportManifestErrorsItem] | Unset):
        breaking_changes (list[str] | Unset):
        items_valid (int | Unset):
        rejected (list[EdpItemRejection] | Unset):
    """

    manifest_valid: bool | None | Unset = UNSET
    manifest_errors: list[EdpValidationReportManifestErrorsItem] | Unset = UNSET
    breaking_changes: list[str] | Unset = UNSET
    items_valid: int | Unset = UNSET
    rejected: list[EdpItemRejection] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        manifest_valid: bool | None | Unset
        if isinstance(self.manifest_valid, Unset):
            manifest_valid = UNSET
        else:
            manifest_valid = self.manifest_valid

        manifest_errors: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.manifest_errors, Unset):
            manifest_errors = []
            for manifest_errors_item_data in self.manifest_errors:
                manifest_errors_item = manifest_errors_item_data.to_dict()
                manifest_errors.append(manifest_errors_item)

        breaking_changes: list[str] | Unset = UNSET
        if not isinstance(self.breaking_changes, Unset):
            breaking_changes = self.breaking_changes

        items_valid = self.items_valid

        rejected: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.rejected, Unset):
            rejected = []
            for rejected_item_data in self.rejected:
                rejected_item = rejected_item_data.to_dict()
                rejected.append(rejected_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if manifest_valid is not UNSET:
            field_dict["manifestValid"] = manifest_valid
        if manifest_errors is not UNSET:
            field_dict["manifestErrors"] = manifest_errors
        if breaking_changes is not UNSET:
            field_dict["breakingChanges"] = breaking_changes
        if items_valid is not UNSET:
            field_dict["itemsValid"] = items_valid
        if rejected is not UNSET:
            field_dict["rejected"] = rejected

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_item_rejection import EdpItemRejection
        from ..models.edp_validation_report_manifest_errors_item import (
            EdpValidationReportManifestErrorsItem,
        )

        d = dict(src_dict)

        def _parse_manifest_valid(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        manifest_valid = _parse_manifest_valid(d.pop("manifestValid", UNSET))

        _manifest_errors = d.pop("manifestErrors", UNSET)
        manifest_errors: list[EdpValidationReportManifestErrorsItem] | Unset = UNSET
        if _manifest_errors is not UNSET:
            manifest_errors = []
            for manifest_errors_item_data in _manifest_errors:
                manifest_errors_item = EdpValidationReportManifestErrorsItem.from_dict(
                    manifest_errors_item_data
                )

                manifest_errors.append(manifest_errors_item)

        breaking_changes = cast(list[str], d.pop("breakingChanges", UNSET))

        items_valid = d.pop("itemsValid", UNSET)

        _rejected = d.pop("rejected", UNSET)
        rejected: list[EdpItemRejection] | Unset = UNSET
        if _rejected is not UNSET:
            rejected = []
            for rejected_item_data in _rejected:
                rejected_item = EdpItemRejection.from_dict(rejected_item_data)

                rejected.append(rejected_item)

        edp_validation_report = cls(
            manifest_valid=manifest_valid,
            manifest_errors=manifest_errors,
            breaking_changes=breaking_changes,
            items_valid=items_valid,
            rejected=rejected,
        )

        edp_validation_report.additional_properties = d
        return edp_validation_report

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
