from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.external_question_link_input_orientation import (
    ExternalQuestionLinkInputOrientation,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ExternalQuestionLinkInput")


@_attrs_define
class ExternalQuestionLinkInput:
    """Map a provider question id to a ledger question (138)

    Attributes:
        stream_id (str):
        external_question_id (str):
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        orientation (ExternalQuestionLinkInputOrientation | Unset):  Default: ExternalQuestionLinkInputOrientation.SAME.
    """

    stream_id: str
    external_question_id: str
    question_id: str
    orientation: ExternalQuestionLinkInputOrientation | Unset = (
        ExternalQuestionLinkInputOrientation.SAME
    )
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        stream_id = self.stream_id

        external_question_id = self.external_question_id

        question_id = self.question_id

        orientation: str | Unset = UNSET
        if not isinstance(self.orientation, Unset):
            orientation = self.orientation.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "streamId": stream_id,
                "externalQuestionId": external_question_id,
                "questionId": question_id,
            }
        )
        if orientation is not UNSET:
            field_dict["orientation"] = orientation

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        stream_id = d.pop("streamId")

        external_question_id = d.pop("externalQuestionId")

        question_id = d.pop("questionId")

        _orientation = d.pop("orientation", UNSET)
        orientation: ExternalQuestionLinkInputOrientation | Unset
        if isinstance(_orientation, Unset):
            orientation = UNSET
        else:
            orientation = ExternalQuestionLinkInputOrientation(_orientation)

        external_question_link_input = cls(
            stream_id=stream_id,
            external_question_id=external_question_id,
            question_id=question_id,
            orientation=orientation,
        )

        external_question_link_input.additional_properties = d
        return external_question_link_input

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
