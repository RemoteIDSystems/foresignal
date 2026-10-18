from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ratings_import_result_provider import RatingsImportResultProvider
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ratings_import_result_errors_item import RatingsImportResultErrorsItem


T = TypeVar("T", bound="RatingsImportResult")


@_attrs_define
class RatingsImportResult:
    """Outcome of a ratings import

    Attributes:
        provider (RatingsImportResultProvider):
        imported (int):
        updated (int):
        rejected (int):
        imported_at (datetime.datetime):
        outlets_created (int | Unset):
        errors (list[RatingsImportResultErrorsItem] | Unset):
    """

    provider: RatingsImportResultProvider
    imported: int
    updated: int
    rejected: int
    imported_at: datetime.datetime
    outlets_created: int | Unset = UNSET
    errors: list[RatingsImportResultErrorsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        provider = self.provider.value

        imported = self.imported

        updated = self.updated

        rejected = self.rejected

        imported_at = self.imported_at.isoformat()

        outlets_created = self.outlets_created

        errors: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.errors, Unset):
            errors = []
            for errors_item_data in self.errors:
                errors_item = errors_item_data.to_dict()
                errors.append(errors_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "provider": provider,
                "imported": imported,
                "updated": updated,
                "rejected": rejected,
                "importedAt": imported_at,
            }
        )
        if outlets_created is not UNSET:
            field_dict["outletsCreated"] = outlets_created
        if errors is not UNSET:
            field_dict["errors"] = errors

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ratings_import_result_errors_item import (
            RatingsImportResultErrorsItem,
        )

        d = dict(src_dict)
        provider = RatingsImportResultProvider(d.pop("provider"))

        imported = d.pop("imported")

        updated = d.pop("updated")

        rejected = d.pop("rejected")

        imported_at = datetime.datetime.fromisoformat(d.pop("importedAt"))

        outlets_created = d.pop("outletsCreated", UNSET)

        _errors = d.pop("errors", UNSET)
        errors: list[RatingsImportResultErrorsItem] | Unset = UNSET
        if _errors is not UNSET:
            errors = []
            for errors_item_data in _errors:
                errors_item = RatingsImportResultErrorsItem.from_dict(errors_item_data)

                errors.append(errors_item)

        ratings_import_result = cls(
            provider=provider,
            imported=imported,
            updated=updated,
            rejected=rejected,
            imported_at=imported_at,
            outlets_created=outlets_created,
            errors=errors,
        )

        ratings_import_result.additional_properties = d
        return ratings_import_result

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
