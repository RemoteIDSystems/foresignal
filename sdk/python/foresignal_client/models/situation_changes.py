from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.situation_changes_area import SituationChangesArea
    from ..models.situation_changes_key_values_item import SituationChangesKeyValuesItem
    from ..models.situation_changes_new_members import SituationChangesNewMembers
    from ..models.situation_changes_severity import SituationChangesSeverity
    from ..models.situation_changes_status import SituationChangesStatus


T = TypeVar("T", bound="SituationChanges")


@_attrs_define
class SituationChanges:
    """What changed since a day: diff of the daily snapshot and the current state (no LLM)

    Attributes:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime): Requested time; the snapshot of its UTC day is the baseline
        now (datetime.datetime):
        status (SituationChangesStatus): Status before and after
        severity (SituationChangesSeverity): Severity before and after
        new_members (SituationChangesNewMembers): Members added since the baseline
        regions_added (list[str]):
        baseline_day (datetime.date | None | Unset): Snapshot day used; null when the situation started after `since`
        area (SituationChangesArea | Unset): Area of the situation geometry
        regions_removed (list[str] | Unset):
        key_values (list[SituationChangesKeyValuesItem] | Unset):
        manifest_id (None | str | Unset):
    """

    situation_id: str
    since: datetime.datetime
    now: datetime.datetime
    status: SituationChangesStatus
    severity: SituationChangesSeverity
    new_members: SituationChangesNewMembers
    regions_added: list[str]
    baseline_day: datetime.date | None | Unset = UNSET
    area: SituationChangesArea | Unset = UNSET
    regions_removed: list[str] | Unset = UNSET
    key_values: list[SituationChangesKeyValuesItem] | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        situation_id = self.situation_id

        since = self.since.isoformat()

        now = self.now.isoformat()

        status = self.status.to_dict()

        severity = self.severity.to_dict()

        new_members = self.new_members.to_dict()

        regions_added = self.regions_added

        baseline_day: None | str | Unset
        if isinstance(self.baseline_day, Unset):
            baseline_day = UNSET
        elif isinstance(self.baseline_day, datetime.date):
            baseline_day = self.baseline_day.isoformat()
        else:
            baseline_day = self.baseline_day

        area: dict[str, Any] | Unset = UNSET
        if not isinstance(self.area, Unset):
            area = self.area.to_dict()

        regions_removed: list[str] | Unset = UNSET
        if not isinstance(self.regions_removed, Unset):
            regions_removed = self.regions_removed

        key_values: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.key_values, Unset):
            key_values = []
            for key_values_item_data in self.key_values:
                key_values_item = key_values_item_data.to_dict()
                key_values.append(key_values_item)

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "situationId": situation_id,
                "since": since,
                "now": now,
                "status": status,
                "severity": severity,
                "newMembers": new_members,
                "regionsAdded": regions_added,
            }
        )
        if baseline_day is not UNSET:
            field_dict["baselineDay"] = baseline_day
        if area is not UNSET:
            field_dict["area"] = area
        if regions_removed is not UNSET:
            field_dict["regionsRemoved"] = regions_removed
        if key_values is not UNSET:
            field_dict["keyValues"] = key_values
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_changes_area import SituationChangesArea
        from ..models.situation_changes_key_values_item import (
            SituationChangesKeyValuesItem,
        )
        from ..models.situation_changes_new_members import SituationChangesNewMembers
        from ..models.situation_changes_severity import SituationChangesSeverity
        from ..models.situation_changes_status import SituationChangesStatus

        d = dict(src_dict)
        situation_id = d.pop("situationId")

        since = datetime.datetime.fromisoformat(d.pop("since"))

        now = datetime.datetime.fromisoformat(d.pop("now"))

        status = SituationChangesStatus.from_dict(d.pop("status"))

        severity = SituationChangesSeverity.from_dict(d.pop("severity"))

        new_members = SituationChangesNewMembers.from_dict(d.pop("newMembers"))

        regions_added = cast(list[str], d.pop("regionsAdded"))

        def _parse_baseline_day(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                baseline_day_type_0 = datetime.date.fromisoformat(data)

                return baseline_day_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        baseline_day = _parse_baseline_day(d.pop("baselineDay", UNSET))

        _area = d.pop("area", UNSET)
        area: SituationChangesArea | Unset
        if isinstance(_area, Unset):
            area = UNSET
        else:
            area = SituationChangesArea.from_dict(_area)

        regions_removed = cast(list[str], d.pop("regionsRemoved", UNSET))

        _key_values = d.pop("keyValues", UNSET)
        key_values: list[SituationChangesKeyValuesItem] | Unset = UNSET
        if _key_values is not UNSET:
            key_values = []
            for key_values_item_data in _key_values:
                key_values_item = SituationChangesKeyValuesItem.from_dict(
                    key_values_item_data
                )

                key_values.append(key_values_item)

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        situation_changes = cls(
            situation_id=situation_id,
            since=since,
            now=now,
            status=status,
            severity=severity,
            new_members=new_members,
            regions_added=regions_added,
            baseline_day=baseline_day,
            area=area,
            regions_removed=regions_removed,
            key_values=key_values,
            manifest_id=manifest_id,
        )

        situation_changes.additional_properties = d
        return situation_changes

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
