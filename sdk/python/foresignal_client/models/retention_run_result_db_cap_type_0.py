from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.retention_run_result_db_cap_type_0_deleted import (
        RetentionRunResultDbCapType0Deleted,
    )


T = TypeVar("T", bound="RetentionRunResultDbCapType0")


@_attrs_define
class RetentionRunResultDbCapType0:
    """What the database-size cap did this run (R2-015); null when it is off

    Attributes:
        cap_bytes (int | Unset):
        size_before_bytes (int | Unset): Database size after the window pass
        size_after_bytes (int | Unset):
        bytes_reclaimed (int | Unset): How far the size fell during the cap steps. Deleted rows free space Postgres
            reuses; files shrink only on a partition drop or VACUUM FULL.
        window_days (int | None | Unset): History window in force at the end, or null for the configured windows
        steps (int | Unset):
        action (str | Unset):
        deleted (RetentionRunResultDbCapType0Deleted | Unset): Rows deleted by the cap steps, by table
    """

    cap_bytes: int | Unset = UNSET
    size_before_bytes: int | Unset = UNSET
    size_after_bytes: int | Unset = UNSET
    bytes_reclaimed: int | Unset = UNSET
    window_days: int | None | Unset = UNSET
    steps: int | Unset = UNSET
    action: str | Unset = UNSET
    deleted: RetentionRunResultDbCapType0Deleted | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        cap_bytes = self.cap_bytes

        size_before_bytes = self.size_before_bytes

        size_after_bytes = self.size_after_bytes

        bytes_reclaimed = self.bytes_reclaimed

        window_days: int | None | Unset
        if isinstance(self.window_days, Unset):
            window_days = UNSET
        else:
            window_days = self.window_days

        steps = self.steps

        action = self.action

        deleted: dict[str, Any] | Unset = UNSET
        if not isinstance(self.deleted, Unset):
            deleted = self.deleted.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if cap_bytes is not UNSET:
            field_dict["capBytes"] = cap_bytes
        if size_before_bytes is not UNSET:
            field_dict["sizeBeforeBytes"] = size_before_bytes
        if size_after_bytes is not UNSET:
            field_dict["sizeAfterBytes"] = size_after_bytes
        if bytes_reclaimed is not UNSET:
            field_dict["bytesReclaimed"] = bytes_reclaimed
        if window_days is not UNSET:
            field_dict["windowDays"] = window_days
        if steps is not UNSET:
            field_dict["steps"] = steps
        if action is not UNSET:
            field_dict["action"] = action
        if deleted is not UNSET:
            field_dict["deleted"] = deleted

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.retention_run_result_db_cap_type_0_deleted import (
            RetentionRunResultDbCapType0Deleted,
        )

        d = dict(src_dict)
        cap_bytes = d.pop("capBytes", UNSET)

        size_before_bytes = d.pop("sizeBeforeBytes", UNSET)

        size_after_bytes = d.pop("sizeAfterBytes", UNSET)

        bytes_reclaimed = d.pop("bytesReclaimed", UNSET)

        def _parse_window_days(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        window_days = _parse_window_days(d.pop("windowDays", UNSET))

        steps = d.pop("steps", UNSET)

        action = d.pop("action", UNSET)

        _deleted = d.pop("deleted", UNSET)
        deleted: RetentionRunResultDbCapType0Deleted | Unset
        if isinstance(_deleted, Unset):
            deleted = UNSET
        else:
            deleted = RetentionRunResultDbCapType0Deleted.from_dict(_deleted)

        retention_run_result_db_cap_type_0 = cls(
            cap_bytes=cap_bytes,
            size_before_bytes=size_before_bytes,
            size_after_bytes=size_after_bytes,
            bytes_reclaimed=bytes_reclaimed,
            window_days=window_days,
            steps=steps,
            action=action,
            deleted=deleted,
        )

        retention_run_result_db_cap_type_0.additional_properties = d
        return retention_run_result_db_cap_type_0

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
