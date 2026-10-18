from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_suggestion_text_origin import ChronicleSuggestionTextOrigin
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_input import ChronicleInput
    from ..models.chronicle_suggestion_estimate import ChronicleSuggestionEstimate


T = TypeVar("T", bound="ChronicleSuggestion")


@_attrs_define
class ChronicleSuggestion:
    """A draft chronicle (not created) with its storage estimate

    Attributes:
        draft (ChronicleInput): Create a chronicle; it starts in status `proposed` (no capture) until activated
        estimate (ChronicleSuggestionEstimate | Unset): Projected storage of the draft
        rationale (list[str] | Unset):
        text_origin (ChronicleSuggestionTextOrigin | Unset):
    """

    draft: ChronicleInput
    estimate: ChronicleSuggestionEstimate | Unset = UNSET
    rationale: list[str] | Unset = UNSET
    text_origin: ChronicleSuggestionTextOrigin | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        draft = self.draft.to_dict()

        estimate: dict[str, Any] | Unset = UNSET
        if not isinstance(self.estimate, Unset):
            estimate = self.estimate.to_dict()

        rationale: list[str] | Unset = UNSET
        if not isinstance(self.rationale, Unset):
            rationale = self.rationale

        text_origin: str | Unset = UNSET
        if not isinstance(self.text_origin, Unset):
            text_origin = self.text_origin.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "draft": draft,
            }
        )
        if estimate is not UNSET:
            field_dict["estimate"] = estimate
        if rationale is not UNSET:
            field_dict["rationale"] = rationale
        if text_origin is not UNSET:
            field_dict["textOrigin"] = text_origin

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_input import ChronicleInput
        from ..models.chronicle_suggestion_estimate import ChronicleSuggestionEstimate

        d = dict(src_dict)
        draft = ChronicleInput.from_dict(d.pop("draft"))

        _estimate = d.pop("estimate", UNSET)
        estimate: ChronicleSuggestionEstimate | Unset
        if isinstance(_estimate, Unset):
            estimate = UNSET
        else:
            estimate = ChronicleSuggestionEstimate.from_dict(_estimate)

        rationale = cast(list[str], d.pop("rationale", UNSET))

        _text_origin = d.pop("textOrigin", UNSET)
        text_origin: ChronicleSuggestionTextOrigin | Unset
        if isinstance(_text_origin, Unset):
            text_origin = UNSET
        else:
            text_origin = ChronicleSuggestionTextOrigin(_text_origin)

        chronicle_suggestion = cls(
            draft=draft,
            estimate=estimate,
            rationale=rationale,
            text_origin=text_origin,
        )

        chronicle_suggestion.additional_properties = d
        return chronicle_suggestion

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
