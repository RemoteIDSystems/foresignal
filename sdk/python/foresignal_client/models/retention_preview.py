from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.retention_preview_db_cap_type_0 import RetentionPreviewDbCapType0
    from ..models.retention_preview_item import RetentionPreviewItem


T = TypeVar("T", bound="RetentionPreview")


@_attrs_define
class RetentionPreview:
    """A dry run of what the next retention run would prune under the current policy, computed with the run's own plans and
    selects as counts only; nothing is deleted (B2/178, R2-015)

        Attributes:
            items (list[RetentionPreviewItem]):
            total_affected (int): Sum of the items' counts
            generated_at (datetime.datetime):
            total_bytes_estimate (int | None | Unset): Sum of the items' byte estimates
            db_cap (None | RetentionPreviewDbCapType0 | Unset): The database-size cap as the next run would find it; null
                when it is off
    """

    items: list[RetentionPreviewItem]
    total_affected: int
    generated_at: datetime.datetime
    total_bytes_estimate: int | None | Unset = UNSET
    db_cap: None | RetentionPreviewDbCapType0 | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.retention_preview_db_cap_type_0 import RetentionPreviewDbCapType0

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        total_affected = self.total_affected

        generated_at = self.generated_at.isoformat()

        total_bytes_estimate: int | None | Unset
        if isinstance(self.total_bytes_estimate, Unset):
            total_bytes_estimate = UNSET
        else:
            total_bytes_estimate = self.total_bytes_estimate

        db_cap: dict[str, Any] | None | Unset
        if isinstance(self.db_cap, Unset):
            db_cap = UNSET
        elif isinstance(self.db_cap, RetentionPreviewDbCapType0):
            db_cap = self.db_cap.to_dict()
        else:
            db_cap = self.db_cap

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "items": items,
                "totalAffected": total_affected,
                "generatedAt": generated_at,
            }
        )
        if total_bytes_estimate is not UNSET:
            field_dict["totalBytesEstimate"] = total_bytes_estimate
        if db_cap is not UNSET:
            field_dict["dbCap"] = db_cap

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.retention_preview_db_cap_type_0 import RetentionPreviewDbCapType0
        from ..models.retention_preview_item import RetentionPreviewItem

        d = dict(src_dict)
        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = RetentionPreviewItem.from_dict(items_item_data)

            items.append(items_item)

        total_affected = d.pop("totalAffected")

        generated_at = datetime.datetime.fromisoformat(d.pop("generatedAt"))

        def _parse_total_bytes_estimate(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        total_bytes_estimate = _parse_total_bytes_estimate(
            d.pop("totalBytesEstimate", UNSET)
        )

        def _parse_db_cap(data: object) -> None | RetentionPreviewDbCapType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                db_cap_type_0 = RetentionPreviewDbCapType0.from_dict(data)

                return db_cap_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RetentionPreviewDbCapType0 | Unset, data)

        db_cap = _parse_db_cap(d.pop("dbCap", UNSET))

        retention_preview = cls(
            items=items,
            total_affected=total_affected,
            generated_at=generated_at,
            total_bytes_estimate=total_bytes_estimate,
            db_cap=db_cap,
        )

        retention_preview.additional_properties = d
        return retention_preview

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
