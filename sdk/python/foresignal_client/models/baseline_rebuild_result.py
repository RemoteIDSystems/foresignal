from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="BaselineRebuildResult")


@_attrs_define
class BaselineRebuildResult:
    """Accepted baseline rebuild

    Attributes:
        rule_id (UUID):
        accepted (bool):
        weeks_available (float | Unset):
        backfill_from (datetime.datetime | None | Unset):
    """

    rule_id: UUID
    accepted: bool
    weeks_available: float | Unset = UNSET
    backfill_from: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rule_id = str(self.rule_id)

        accepted = self.accepted

        weeks_available = self.weeks_available

        backfill_from: None | str | Unset
        if isinstance(self.backfill_from, Unset):
            backfill_from = UNSET
        elif isinstance(self.backfill_from, datetime.datetime):
            backfill_from = self.backfill_from.isoformat()
        else:
            backfill_from = self.backfill_from

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ruleId": rule_id,
                "accepted": accepted,
            }
        )
        if weeks_available is not UNSET:
            field_dict["weeksAvailable"] = weeks_available
        if backfill_from is not UNSET:
            field_dict["backfillFrom"] = backfill_from

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        rule_id = UUID(d.pop("ruleId"))

        accepted = d.pop("accepted")

        weeks_available = d.pop("weeksAvailable", UNSET)

        def _parse_backfill_from(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                backfill_from_type_0 = datetime.datetime.fromisoformat(data)

                return backfill_from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        backfill_from = _parse_backfill_from(d.pop("backfillFrom", UNSET))

        baseline_rebuild_result = cls(
            rule_id=rule_id,
            accepted=accepted,
            weeks_available=weeks_available,
            backfill_from=backfill_from,
        )

        baseline_rebuild_result.additional_properties = d
        return baseline_rebuild_result

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
