from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.anomaly_baseline_bucket import AnomalyBaselineBucket
from ..models.anomaly_baseline_sample_path import AnomalyBaselineSamplePath
from ..models.anomaly_baseline_slot_mode import AnomalyBaselineSlotMode
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.anomaly_baseline_slot import AnomalyBaselineSlot


T = TypeVar("T", bound="AnomalyBaseline")


@_attrs_define
class AnomalyBaseline:
    """Learned baseline of an anomaly rule

    Attributes:
        rule_id (UUID):
        slot_mode (AnomalyBaselineSlotMode):
        bucket (AnomalyBaselineBucket):
        weeks_available (float):
        learning (bool):
        slots (list[AnomalyBaselineSlot]):
        min_weeks (int | Unset):
        learning_until (datetime.datetime | None | Unset):
        low_confidence (bool | Unset):
        sample_path (AnomalyBaselineSamplePath | Unset): Which store the samples come from
    """

    rule_id: UUID
    slot_mode: AnomalyBaselineSlotMode
    bucket: AnomalyBaselineBucket
    weeks_available: float
    learning: bool
    slots: list[AnomalyBaselineSlot]
    min_weeks: int | Unset = UNSET
    learning_until: datetime.datetime | None | Unset = UNSET
    low_confidence: bool | Unset = UNSET
    sample_path: AnomalyBaselineSamplePath | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rule_id = str(self.rule_id)

        slot_mode = self.slot_mode.value

        bucket = self.bucket.value

        weeks_available = self.weeks_available

        learning = self.learning

        slots = []
        for slots_item_data in self.slots:
            slots_item = slots_item_data.to_dict()
            slots.append(slots_item)

        min_weeks = self.min_weeks

        learning_until: None | str | Unset
        if isinstance(self.learning_until, Unset):
            learning_until = UNSET
        elif isinstance(self.learning_until, datetime.datetime):
            learning_until = self.learning_until.isoformat()
        else:
            learning_until = self.learning_until

        low_confidence = self.low_confidence

        sample_path: str | Unset = UNSET
        if not isinstance(self.sample_path, Unset):
            sample_path = self.sample_path.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ruleId": rule_id,
                "slotMode": slot_mode,
                "bucket": bucket,
                "weeksAvailable": weeks_available,
                "learning": learning,
                "slots": slots,
            }
        )
        if min_weeks is not UNSET:
            field_dict["minWeeks"] = min_weeks
        if learning_until is not UNSET:
            field_dict["learningUntil"] = learning_until
        if low_confidence is not UNSET:
            field_dict["lowConfidence"] = low_confidence
        if sample_path is not UNSET:
            field_dict["samplePath"] = sample_path

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.anomaly_baseline_slot import AnomalyBaselineSlot

        d = dict(src_dict)
        rule_id = UUID(d.pop("ruleId"))

        slot_mode = AnomalyBaselineSlotMode(d.pop("slotMode"))

        bucket = AnomalyBaselineBucket(d.pop("bucket"))

        weeks_available = d.pop("weeksAvailable")

        learning = d.pop("learning")

        slots = []
        _slots = d.pop("slots")
        for slots_item_data in _slots:
            slots_item = AnomalyBaselineSlot.from_dict(slots_item_data)

            slots.append(slots_item)

        min_weeks = d.pop("minWeeks", UNSET)

        def _parse_learning_until(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                learning_until_type_0 = datetime.datetime.fromisoformat(data)

                return learning_until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        learning_until = _parse_learning_until(d.pop("learningUntil", UNSET))

        low_confidence = d.pop("lowConfidence", UNSET)

        _sample_path = d.pop("samplePath", UNSET)
        sample_path: AnomalyBaselineSamplePath | Unset
        if isinstance(_sample_path, Unset):
            sample_path = UNSET
        else:
            sample_path = AnomalyBaselineSamplePath(_sample_path)

        anomaly_baseline = cls(
            rule_id=rule_id,
            slot_mode=slot_mode,
            bucket=bucket,
            weeks_available=weeks_available,
            learning=learning,
            slots=slots,
            min_weeks=min_weeks,
            learning_until=learning_until,
            low_confidence=low_confidence,
            sample_path=sample_path,
        )

        anomaly_baseline.additional_properties = d
        return anomaly_baseline

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
