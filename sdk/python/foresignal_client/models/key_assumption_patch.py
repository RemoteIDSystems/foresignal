from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.key_assumption_patch_status import KeyAssumptionPatchStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="KeyAssumptionPatch")


@_attrs_define
class KeyAssumptionPatch:
    """JSON merge patch for a key assumption; `broken` emits `assumption.broken`

    Attributes:
        text (str | Unset):
        linchpin (bool | Unset):
        rationale (None | str | Unset):
        would_be_wrong_if (None | str | Unset):
        status (KeyAssumptionPatchStatus | Unset):
        indicator_ids (list[str] | Unset):
        review_due (datetime.datetime | None | Unset):
    """

    text: str | Unset = UNSET
    linchpin: bool | Unset = UNSET
    rationale: None | str | Unset = UNSET
    would_be_wrong_if: None | str | Unset = UNSET
    status: KeyAssumptionPatchStatus | Unset = UNSET
    indicator_ids: list[str] | Unset = UNSET
    review_due: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        text = self.text

        linchpin = self.linchpin

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

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

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

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if text is not UNSET:
            field_dict["text"] = text
        if linchpin is not UNSET:
            field_dict["linchpin"] = linchpin
        if rationale is not UNSET:
            field_dict["rationale"] = rationale
        if would_be_wrong_if is not UNSET:
            field_dict["wouldBeWrongIf"] = would_be_wrong_if
        if status is not UNSET:
            field_dict["status"] = status
        if indicator_ids is not UNSET:
            field_dict["indicatorIds"] = indicator_ids
        if review_due is not UNSET:
            field_dict["reviewDue"] = review_due

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        text = d.pop("text", UNSET)

        linchpin = d.pop("linchpin", UNSET)

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

        _status = d.pop("status", UNSET)
        status: KeyAssumptionPatchStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = KeyAssumptionPatchStatus(_status)

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

        key_assumption_patch = cls(
            text=text,
            linchpin=linchpin,
            rationale=rationale,
            would_be_wrong_if=would_be_wrong_if,
            status=status,
            indicator_ids=indicator_ids,
            review_due=review_due,
        )

        key_assumption_patch.additional_properties = d
        return key_assumption_patch

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
