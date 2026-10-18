from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.pol_deviation_deviation_type import PolDeviationDeviationType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.pol_deviation_stay_type_0 import PolDeviationStayType0


T = TypeVar("T", bound="PolDeviation")


@_attrs_define
class PolDeviation:
    """Payload of `pol-deviation` records of the derived source pattern-of-life (114)

    Attributes:
        subject_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        deviation_type (PolDeviationDeviationType):
        at (datetime.datetime):
        subject_ref (str | Unset):
        place_id (None | str | Unset):
        place_label (None | str | Unset):
        stay (None | PolDeviationStayType0 | Unset):
        hour_of_week (int | Unset):
        bucket_share (float | None | Unset): Share of the place's dwell in this hour-of-week bucket (UNUSUAL_TIME <
            0.02; EXPECTED_ABSENT ≥ 0.6)
        last_seen_at (datetime.datetime | None | Unset):
    """

    subject_id: str
    deviation_type: PolDeviationDeviationType
    at: datetime.datetime
    subject_ref: str | Unset = UNSET
    place_id: None | str | Unset = UNSET
    place_label: None | str | Unset = UNSET
    stay: None | PolDeviationStayType0 | Unset = UNSET
    hour_of_week: int | Unset = UNSET
    bucket_share: float | None | Unset = UNSET
    last_seen_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.pol_deviation_stay_type_0 import PolDeviationStayType0

        subject_id = self.subject_id

        deviation_type = self.deviation_type.value

        at = self.at.isoformat()

        subject_ref = self.subject_ref

        place_id: None | str | Unset
        if isinstance(self.place_id, Unset):
            place_id = UNSET
        else:
            place_id = self.place_id

        place_label: None | str | Unset
        if isinstance(self.place_label, Unset):
            place_label = UNSET
        else:
            place_label = self.place_label

        stay: dict[str, Any] | None | Unset
        if isinstance(self.stay, Unset):
            stay = UNSET
        elif isinstance(self.stay, PolDeviationStayType0):
            stay = self.stay.to_dict()
        else:
            stay = self.stay

        hour_of_week = self.hour_of_week

        bucket_share: float | None | Unset
        if isinstance(self.bucket_share, Unset):
            bucket_share = UNSET
        else:
            bucket_share = self.bucket_share

        last_seen_at: None | str | Unset
        if isinstance(self.last_seen_at, Unset):
            last_seen_at = UNSET
        elif isinstance(self.last_seen_at, datetime.datetime):
            last_seen_at = self.last_seen_at.isoformat()
        else:
            last_seen_at = self.last_seen_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "subjectId": subject_id,
                "deviationType": deviation_type,
                "at": at,
            }
        )
        if subject_ref is not UNSET:
            field_dict["subjectRef"] = subject_ref
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if place_label is not UNSET:
            field_dict["placeLabel"] = place_label
        if stay is not UNSET:
            field_dict["stay"] = stay
        if hour_of_week is not UNSET:
            field_dict["hourOfWeek"] = hour_of_week
        if bucket_share is not UNSET:
            field_dict["bucketShare"] = bucket_share
        if last_seen_at is not UNSET:
            field_dict["lastSeenAt"] = last_seen_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.pol_deviation_stay_type_0 import PolDeviationStayType0

        d = dict(src_dict)
        subject_id = d.pop("subjectId")

        deviation_type = PolDeviationDeviationType(d.pop("deviationType"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        subject_ref = d.pop("subjectRef", UNSET)

        def _parse_place_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        place_id = _parse_place_id(d.pop("placeId", UNSET))

        def _parse_place_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        place_label = _parse_place_label(d.pop("placeLabel", UNSET))

        def _parse_stay(data: object) -> None | PolDeviationStayType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                stay_type_0 = PolDeviationStayType0.from_dict(data)

                return stay_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PolDeviationStayType0 | Unset, data)

        stay = _parse_stay(d.pop("stay", UNSET))

        hour_of_week = d.pop("hourOfWeek", UNSET)

        def _parse_bucket_share(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        bucket_share = _parse_bucket_share(d.pop("bucketShare", UNSET))

        def _parse_last_seen_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_seen_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_seen_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_seen_at = _parse_last_seen_at(d.pop("lastSeenAt", UNSET))

        pol_deviation = cls(
            subject_id=subject_id,
            deviation_type=deviation_type,
            at=at,
            subject_ref=subject_ref,
            place_id=place_id,
            place_label=place_label,
            stay=stay,
            hour_of_week=hour_of_week,
            bucket_share=bucket_share,
            last_seen_at=last_seen_at,
        )

        pol_deviation.additional_properties = d
        return pol_deviation

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
