from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.exercise_run_create_speed import ExerciseRunCreateSpeed
from ..types import UNSET, Unset

T = TypeVar("T", bound="ExerciseRunCreate")


@_attrs_define
class ExerciseRunCreate:
    """Create and start an exercise replay, or project it with dryRun (115)

    Attributes:
        name (str):
        rule_ids (list[str] | Unset): Rules to replay (or use tag)
        tag (None | str | Unset):
        from_ (datetime.datetime | None | Unset):
        to (datetime.datetime | None | Unset):
        from_seq (int | None | Unset):
        to_seq (int | None | Unset):
        speed (ExerciseRunCreateSpeed | Unset):
        channel_ids (list[str] | Unset): Channels to deliver to, in addition to channels with acceptExercise=true
        enrich (bool | Unset): Allow 030 enrichment of exercise alerts Default: False.
        dry_run (bool | Unset): Return projected counts per rule and channel without delivering Default: False.
    """

    name: str
    rule_ids: list[str] | Unset = UNSET
    tag: None | str | Unset = UNSET
    from_: datetime.datetime | None | Unset = UNSET
    to: datetime.datetime | None | Unset = UNSET
    from_seq: int | None | Unset = UNSET
    to_seq: int | None | Unset = UNSET
    speed: ExerciseRunCreateSpeed | Unset = UNSET
    channel_ids: list[str] | Unset = UNSET
    enrich: bool | Unset = False
    dry_run: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

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

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
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

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

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
        speed: ExerciseRunCreateSpeed | Unset
        if isinstance(_speed, Unset):
            speed = UNSET
        else:
            speed = ExerciseRunCreateSpeed(_speed)

        channel_ids = cast(list[str], d.pop("channelIds", UNSET))

        enrich = d.pop("enrich", UNSET)

        dry_run = d.pop("dryRun", UNSET)

        exercise_run_create = cls(
            name=name,
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
        )

        exercise_run_create.additional_properties = d
        return exercise_run_create

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
