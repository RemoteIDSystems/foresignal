from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.question_annulment_code import QuestionAnnulmentCode

T = TypeVar("T", bound="QuestionAnnulment")


@_attrs_define
class QuestionAnnulment:
    """Why a question was annulled (annulled questions are not scored)

    Attributes:
        code (QuestionAnnulmentCode):
        reason (str):
        at (datetime.datetime):
        by (str):
    """

    code: QuestionAnnulmentCode
    reason: str
    at: datetime.datetime
    by: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        code = self.code.value

        reason = self.reason

        at = self.at.isoformat()

        by = self.by

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "code": code,
                "reason": reason,
                "at": at,
                "by": by,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        code = QuestionAnnulmentCode(d.pop("code"))

        reason = d.pop("reason")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        by = d.pop("by")

        question_annulment = cls(
            code=code,
            reason=reason,
            at=at,
            by=by,
        )

        question_annulment.additional_properties = d
        return question_annulment

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
