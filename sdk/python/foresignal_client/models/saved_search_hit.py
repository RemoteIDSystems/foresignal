from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.saved_search_hit_matched_by import SavedSearchHitMatchedBy
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.object_envelope import ObjectEnvelope


T = TypeVar("T", bound="SavedSearchHit")


@_attrs_define
class SavedSearchHit:
    """One saved-search match

    Attributes:
        envelope (ObjectEnvelope): Uniform summary of any Foresignal object (docs/Client-Contract.md §3)
        score (float | None | Unset):
        matched_by (SavedSearchHitMatchedBy | Unset):
    """

    envelope: ObjectEnvelope
    score: float | None | Unset = UNSET
    matched_by: SavedSearchHitMatchedBy | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        envelope = self.envelope.to_dict()

        score: float | None | Unset
        if isinstance(self.score, Unset):
            score = UNSET
        else:
            score = self.score

        matched_by: str | Unset = UNSET
        if not isinstance(self.matched_by, Unset):
            matched_by = self.matched_by.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "envelope": envelope,
            }
        )
        if score is not UNSET:
            field_dict["score"] = score
        if matched_by is not UNSET:
            field_dict["matchedBy"] = matched_by

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.object_envelope import ObjectEnvelope

        d = dict(src_dict)
        envelope = ObjectEnvelope.from_dict(d.pop("envelope"))

        def _parse_score(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        score = _parse_score(d.pop("score", UNSET))

        _matched_by = d.pop("matchedBy", UNSET)
        matched_by: SavedSearchHitMatchedBy | Unset
        if isinstance(_matched_by, Unset):
            matched_by = UNSET
        else:
            matched_by = SavedSearchHitMatchedBy(_matched_by)

        saved_search_hit = cls(
            envelope=envelope,
            score=score,
            matched_by=matched_by,
        )

        saved_search_hit.additional_properties = d
        return saved_search_hit

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
