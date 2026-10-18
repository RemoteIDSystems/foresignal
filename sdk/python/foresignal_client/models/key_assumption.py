from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.key_assumption_status import KeyAssumptionStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="KeyAssumption")


@_attrs_define
class KeyAssumption:
    """A key assumption behind a hypothesis set

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        text (str):
        linchpin (bool):
        status (KeyAssumptionStatus):
        updated_at (datetime.datetime):
        rationale (None | str | Unset):
        would_be_wrong_if (None | str | Unset):
        indicator_ids (list[str] | Unset):
        review_due (datetime.datetime | None | Unset):
        updated_by (None | str | Unset):
    """

    id: str
    set_id: str
    text: str
    linchpin: bool
    status: KeyAssumptionStatus
    updated_at: datetime.datetime
    rationale: None | str | Unset = UNSET
    would_be_wrong_if: None | str | Unset = UNSET
    indicator_ids: list[str] | Unset = UNSET
    review_due: datetime.datetime | None | Unset = UNSET
    updated_by: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        set_id = self.set_id

        text = self.text

        linchpin = self.linchpin

        status = self.status.value

        updated_at = self.updated_at.isoformat()

        rationale: None | str | Unset
        if isinstance(self.rationale, Unset):
            rationale = UNSET
        else:
            rationale = self.rationale

        would_be_wrong_if: None | str | Unset
        if isinstance(self.would_be_wrong_if, Unset):
            would_be_wrong_if = UNSET
        else:
            would_be_wrong_if = self.would_be_wrong_if

        indicator_ids: list[str] | Unset = UNSET
        if not isinstance(self.indicator_ids, Unset):
            indicator_ids = self.indicator_ids

        review_due: None | str | Unset
        if isinstance(self.review_due, Unset):
            review_due = UNSET
        elif isinstance(self.review_due, datetime.datetime):
            review_due = self.review_due.isoformat()
        else:
            review_due = self.review_due

        updated_by: None | str | Unset
        if isinstance(self.updated_by, Unset):
            updated_by = UNSET
        else:
            updated_by = self.updated_by

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "setId": set_id,
                "text": text,
                "linchpin": linchpin,
                "status": status,
                "updatedAt": updated_at,
            }
        )
        if rationale is not UNSET:
            field_dict["rationale"] = rationale
        if would_be_wrong_if is not UNSET:
            field_dict["wouldBeWrongIf"] = would_be_wrong_if
        if indicator_ids is not UNSET:
            field_dict["indicatorIds"] = indicator_ids
        if review_due is not UNSET:
            field_dict["reviewDue"] = review_due
        if updated_by is not UNSET:
            field_dict["updatedBy"] = updated_by

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        set_id = d.pop("setId")

        text = d.pop("text")

        linchpin = d.pop("linchpin")

        status = KeyAssumptionStatus(d.pop("status"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        def _parse_rationale(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        rationale = _parse_rationale(d.pop("rationale", UNSET))

        def _parse_would_be_wrong_if(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        would_be_wrong_if = _parse_would_be_wrong_if(d.pop("wouldBeWrongIf", UNSET))

        indicator_ids = cast(list[str], d.pop("indicatorIds", UNSET))

        def _parse_review_due(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                review_due_type_0 = datetime.datetime.fromisoformat(data)

                return review_due_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        review_due = _parse_review_due(d.pop("reviewDue", UNSET))

        def _parse_updated_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        updated_by = _parse_updated_by(d.pop("updatedBy", UNSET))

        key_assumption = cls(
            id=id,
            set_id=set_id,
            text=text,
            linchpin=linchpin,
            status=status,
            updated_at=updated_at,
            rationale=rationale,
            would_be_wrong_if=would_be_wrong_if,
            indicator_ids=indicator_ids,
            review_due=review_due,
            updated_by=updated_by,
        )

        key_assumption.additional_properties = d
        return key_assumption

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
