from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.event_study_result_events_item_abnormal_item import (
        EventStudyResultEventsItemAbnormalItem,
    )
    from ..models.event_study_result_events_item_car import (
        EventStudyResultEventsItemCar,
    )


T = TypeVar("T", bound="EventStudyResultEventsItem")


@_attrs_define
class EventStudyResultEventsItem:
    """One event's abnormal path

    Attributes:
        at (datetime.datetime):
        cluster_id (int):
        ref (None | str | Unset):
        excluded (None | str | Unset): Why the event was excluded, e.g. `FEW_EVENTS` or coverage
        estimation_points (int | Unset):
        abnormal (list[EventStudyResultEventsItemAbnormalItem] | Unset):
        car (EventStudyResultEventsItemCar | Unset): CAR per window
    """

    at: datetime.datetime
    cluster_id: int
    ref: None | str | Unset = UNSET
    excluded: None | str | Unset = UNSET
    estimation_points: int | Unset = UNSET
    abnormal: list[EventStudyResultEventsItemAbnormalItem] | Unset = UNSET
    car: EventStudyResultEventsItemCar | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        at = self.at.isoformat()

        cluster_id = self.cluster_id

        ref: None | str | Unset
        if isinstance(self.ref, Unset):
            ref = UNSET
        else:
            ref = self.ref

        excluded: None | str | Unset
        if isinstance(self.excluded, Unset):
            excluded = UNSET
        else:
            excluded = self.excluded

        estimation_points = self.estimation_points

        abnormal: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.abnormal, Unset):
            abnormal = []
            for abnormal_item_data in self.abnormal:
                abnormal_item = abnormal_item_data.to_dict()
                abnormal.append(abnormal_item)

        car: dict[str, Any] | Unset = UNSET
        if not isinstance(self.car, Unset):
            car = self.car.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "at": at,
                "clusterId": cluster_id,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if excluded is not UNSET:
            field_dict["excluded"] = excluded
        if estimation_points is not UNSET:
            field_dict["estimationPoints"] = estimation_points
        if abnormal is not UNSET:
            field_dict["abnormal"] = abnormal
        if car is not UNSET:
            field_dict["car"] = car

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.event_study_result_events_item_abnormal_item import (
            EventStudyResultEventsItemAbnormalItem,
        )
        from ..models.event_study_result_events_item_car import (
            EventStudyResultEventsItemCar,
        )

        d = dict(src_dict)
        at = datetime.datetime.fromisoformat(d.pop("at"))

        cluster_id = d.pop("clusterId")

        def _parse_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ref = _parse_ref(d.pop("ref", UNSET))

        def _parse_excluded(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        excluded = _parse_excluded(d.pop("excluded", UNSET))

        estimation_points = d.pop("estimationPoints", UNSET)

        _abnormal = d.pop("abnormal", UNSET)
        abnormal: list[EventStudyResultEventsItemAbnormalItem] | Unset = UNSET
        if _abnormal is not UNSET:
            abnormal = []
            for abnormal_item_data in _abnormal:
                abnormal_item = EventStudyResultEventsItemAbnormalItem.from_dict(
                    abnormal_item_data
                )

                abnormal.append(abnormal_item)

        _car = d.pop("car", UNSET)
        car: EventStudyResultEventsItemCar | Unset
        if isinstance(_car, Unset):
            car = UNSET
        else:
            car = EventStudyResultEventsItemCar.from_dict(_car)

        event_study_result_events_item = cls(
            at=at,
            cluster_id=cluster_id,
            ref=ref,
            excluded=excluded,
            estimation_points=estimation_points,
            abnormal=abnormal,
            car=car,
        )

        event_study_result_events_item.additional_properties = d
        return event_study_result_events_item

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
