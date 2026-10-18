from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.exercise_run_speed import ExerciseRunSpeed
from ..models.exercise_run_status import ExerciseRunStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.exercise_stats import ExerciseStats


T = TypeVar("T", bound="ExerciseRun")


@_attrs_define
class ExerciseRun:
    """An exercise replay run (115)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        name (str):
        status (ExerciseRunStatus):
        created_at (datetime.datetime):
        rule_ids (list[str] | Unset):
        tag (None | str | Unset):
        from_ (datetime.datetime | None | Unset):
        to (datetime.datetime | None | Unset):
        from_seq (int | None | Unset):
        to_seq (int | None | Unset):
        speed (ExerciseRunSpeed | Unset):
        channel_ids (list[str] | Unset):
        enrich (bool | Unset):
        dry_run (bool | Unset):
        created_by (str | Unset):
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        stats (ExerciseStats | Unset): Statistics of an exercise run (projected for a dry run) (115)
    """

    id: str
    name: str
    status: ExerciseRunStatus
    created_at: datetime.datetime
    rule_ids: list[str] | Unset = UNSET
    tag: None | str | Unset = UNSET
    from_: datetime.datetime | None | Unset = UNSET
    to: datetime.datetime | None | Unset = UNSET
    from_seq: int | None | Unset = UNSET
    to_seq: int | None | Unset = UNSET
    speed: ExerciseRunSpeed | Unset = UNSET
    channel_ids: list[str] | Unset = UNSET
    enrich: bool | Unset = UNSET
    dry_run: bool | Unset = UNSET
    created_by: str | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    stats: ExerciseStats | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        name = self.name

        status = self.status.value

        created_at = self.created_at.isoformat()

        rule_ids: list[str] | Unset = UNSET
        if not isinstance(self.rule_ids, Unset):
            rule_ids = self.rule_ids

        tag: None | str | Unset
        if isinstance(self.tag, Unset):
            tag = UNSET
        else:
            tag = self.tag

        from_: None | str | Unset
        if isinstance(self.from_, Unset):
            from_ = UNSET
        elif isinstance(self.from_, datetime.datetime):
            from_ = self.from_.isoformat()
        else:
            from_ = self.from_

        to: None | str | Unset
        if isinstance(self.to, Unset):
            to = UNSET
        elif isinstance(self.to, datetime.datetime):
            to = self.to.isoformat()
        else:
            to = self.to

        from_seq: int | None | Unset
        if isinstance(self.from_seq, Unset):
            from_seq = UNSET
        else:
            from_seq = self.from_seq

        to_seq: int | None | Unset
        if isinstance(self.to_seq, Unset):
            to_seq = UNSET
        else:
            to_seq = self.to_seq

        speed: str | Unset = UNSET
        if not isinstance(self.speed, Unset):
            speed = self.speed.value

        channel_ids: list[str] | Unset = UNSET
        if not isinstance(self.channel_ids, Unset):
            channel_ids = self.channel_ids

        enrich = self.enrich

        dry_run = self.dry_run

        created_by = self.created_by

        started_at: None | str | Unset
        if isinstance(self.started_at, Unset):
            started_at = UNSET
        elif isinstance(self.started_at, datetime.datetime):
            started_at = self.started_at.isoformat()
        else:
            started_at = self.started_at

        finished_at: None | str | Unset
        if isinstance(self.finished_at, Unset):
            finished_at = UNSET
        elif isinstance(self.finished_at, datetime.datetime):
            finished_at = self.finished_at.isoformat()
        else:
            finished_at = self.finished_at

        stats: dict[str, Any] | Unset = UNSET
        if not isinstance(self.stats, Unset):
            stats = self.stats.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "status": status,
                "createdAt": created_at,
            }
        )
        if rule_ids is not UNSET:
            field_dict["ruleIds"] = rule_ids
        if tag is not UNSET:
            field_dict["tag"] = tag
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if from_seq is not UNSET:
            field_dict["fromSeq"] = from_seq
        if to_seq is not UNSET:
            field_dict["toSeq"] = to_seq
        if speed is not UNSET:
            field_dict["speed"] = speed
        if channel_ids is not UNSET:
            field_dict["channelIds"] = channel_ids
        if enrich is not UNSET:
            field_dict["enrich"] = enrich
        if dry_run is not UNSET:
            field_dict["dryRun"] = dry_run
        if created_by is not UNSET:
            field_dict["createdBy"] = created_by
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if stats is not UNSET:
            field_dict["stats"] = stats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.exercise_stats import ExerciseStats

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        status = ExerciseRunStatus(d.pop("status"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        rule_ids = cast(list[str], d.pop("ruleIds", UNSET))

        def _parse_tag(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        tag = _parse_tag(d.pop("tag", UNSET))

        def _parse_from_(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                from_type_0 = datetime.datetime.fromisoformat(data)

                return from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        from_ = _parse_from_(d.pop("from", UNSET))

        def _parse_to(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                to_type_0 = datetime.datetime.fromisoformat(data)

                return to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        to = _parse_to(d.pop("to", UNSET))

        def _parse_from_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        from_seq = _parse_from_seq(d.pop("fromSeq", UNSET))

        def _parse_to_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        to_seq = _parse_to_seq(d.pop("toSeq", UNSET))

        _speed = d.pop("speed", UNSET)
        speed: ExerciseRunSpeed | Unset
        if isinstance(_speed, Unset):
            speed = UNSET
        else:
            speed = ExerciseRunSpeed(_speed)

        channel_ids = cast(list[str], d.pop("channelIds", UNSET))

        enrich = d.pop("enrich", UNSET)

        dry_run = d.pop("dryRun", UNSET)

        created_by = d.pop("createdBy", UNSET)

        def _parse_started_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                started_at_type_0 = datetime.datetime.fromisoformat(data)

                return started_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        started_at = _parse_started_at(d.pop("startedAt", UNSET))

        def _parse_finished_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                finished_at_type_0 = datetime.datetime.fromisoformat(data)

                return finished_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        finished_at = _parse_finished_at(d.pop("finishedAt", UNSET))

        _stats = d.pop("stats", UNSET)
        stats: ExerciseStats | Unset
        if isinstance(_stats, Unset):
            stats = UNSET
        else:
            stats = ExerciseStats.from_dict(_stats)

        exercise_run = cls(
            id=id,
            name=name,
            status=status,
            created_at=created_at,
            rule_ids=rule_ids,
            tag=tag,
            from_=from_,
            to=to,
            from_seq=from_seq,
            to_seq=to_seq,
            speed=speed,
            channel_ids=channel_ids,
            enrich=enrich,
            dry_run=dry_run,
            created_by=created_by,
            started_at=started_at,
            finished_at=finished_at,
            stats=stats,
        )

        exercise_run.additional_properties = d
        return exercise_run

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
