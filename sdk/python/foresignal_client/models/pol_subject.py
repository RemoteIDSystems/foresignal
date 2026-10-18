from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.pol_subject_subject_type import PolSubjectSubjectType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.pol_subject_params import PolSubjectParams


T = TypeVar("T", bound="PolSubject")


@_attrs_define
class PolSubject:
    """A pattern-of-life subject (114)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        subject_type (PolSubjectSubjectType):
        ref (str):
        enabled (bool):
        created_at (datetime.datetime):
        params (PolSubjectParams | Unset):
        private (bool | Unset): Outputs inherit private and the precision cap from a private source
        created_by (str | Unset):
        last_seq (int | None | Unset):
        last_computed_at (datetime.datetime | None | Unset):
        place_count (int | Unset):
        stay_count (int | Unset):
    """

    id: str
    subject_type: PolSubjectSubjectType
    ref: str
    enabled: bool
    created_at: datetime.datetime
    params: PolSubjectParams | Unset = UNSET
    private: bool | Unset = UNSET
    created_by: str | Unset = UNSET
    last_seq: int | None | Unset = UNSET
    last_computed_at: datetime.datetime | None | Unset = UNSET
    place_count: int | Unset = UNSET
    stay_count: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        subject_type = self.subject_type.value

        ref = self.ref

        enabled = self.enabled

        created_at = self.created_at.isoformat()

        params: dict[str, Any] | Unset = UNSET
        if not isinstance(self.params, Unset):
            params = self.params.to_dict()

        private = self.private

        created_by = self.created_by

        last_seq: int | None | Unset
        if isinstance(self.last_seq, Unset):
            last_seq = UNSET
        else:
            last_seq = self.last_seq

        last_computed_at: None | str | Unset
        if isinstance(self.last_computed_at, Unset):
            last_computed_at = UNSET
        elif isinstance(self.last_computed_at, datetime.datetime):
            last_computed_at = self.last_computed_at.isoformat()
        else:
            last_computed_at = self.last_computed_at

        place_count = self.place_count

        stay_count = self.stay_count

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "subjectType": subject_type,
                "ref": ref,
                "enabled": enabled,
                "createdAt": created_at,
            }
        )
        if params is not UNSET:
            field_dict["params"] = params
        if private is not UNSET:
            field_dict["private"] = private
        if created_by is not UNSET:
            field_dict["createdBy"] = created_by
        if last_seq is not UNSET:
            field_dict["lastSeq"] = last_seq
        if last_computed_at is not UNSET:
            field_dict["lastComputedAt"] = last_computed_at
        if place_count is not UNSET:
            field_dict["placeCount"] = place_count
        if stay_count is not UNSET:
            field_dict["stayCount"] = stay_count

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.pol_subject_params import PolSubjectParams

        d = dict(src_dict)
        id = d.pop("id")

        subject_type = PolSubjectSubjectType(d.pop("subjectType"))

        ref = d.pop("ref")

        enabled = d.pop("enabled")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        _params = d.pop("params", UNSET)
        params: PolSubjectParams | Unset
        if isinstance(_params, Unset):
            params = UNSET
        else:
            params = PolSubjectParams.from_dict(_params)

        private = d.pop("private", UNSET)

        created_by = d.pop("createdBy", UNSET)

        def _parse_last_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        last_seq = _parse_last_seq(d.pop("lastSeq", UNSET))

        def _parse_last_computed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_computed_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_computed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_computed_at = _parse_last_computed_at(d.pop("lastComputedAt", UNSET))

        place_count = d.pop("placeCount", UNSET)

        stay_count = d.pop("stayCount", UNSET)

        pol_subject = cls(
            id=id,
            subject_type=subject_type,
            ref=ref,
            enabled=enabled,
            created_at=created_at,
            params=params,
            private=private,
            created_by=created_by,
            last_seq=last_seq,
            last_computed_at=last_computed_at,
            place_count=place_count,
            stay_count=stay_count,
        )

        pol_subject.additional_properties = d
        return pol_subject

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
