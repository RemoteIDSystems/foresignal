from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ach_evidence_input_credibility import AchEvidenceInputCredibility
from ..models.ach_evidence_input_relevance import AchEvidenceInputRelevance
from ..models.ach_evidence_input_source import AchEvidenceInputSource
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ach_evidence_input_ratings import AchEvidenceInputRatings


T = TypeVar("T", bound="AchEvidenceInput")


@_attrs_define
class AchEvidenceInput:
    """Manual or referenced evidence to add to a set's ACH matrix

    Attributes:
        source (AchEvidenceInputSource):
        description (str):
        credibility (AchEvidenceInputCredibility):
        relevance (AchEvidenceInputRelevance):
        ref (None | str | Unset): Object reference (required unless `manual`)
        ratings (AchEvidenceInputRatings | Unset): Per hypothesis id; required for manual evidence
        citations (list[str] | Unset):
    """

    source: AchEvidenceInputSource
    description: str
    credibility: AchEvidenceInputCredibility
    relevance: AchEvidenceInputRelevance
    ref: None | str | Unset = UNSET
    ratings: AchEvidenceInputRatings | Unset = UNSET
    citations: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source = self.source.value

        description = self.description

        credibility = self.credibility.value

        relevance = self.relevance.value

        ref: None | str | Unset
        if isinstance(self.ref, Unset):
            ref = UNSET
        else:
            ref = self.ref

        ratings: dict[str, Any] | Unset = UNSET
        if not isinstance(self.ratings, Unset):
            ratings = self.ratings.to_dict()

        citations: list[str] | Unset = UNSET
        if not isinstance(self.citations, Unset):
            citations = self.citations

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "source": source,
                "description": description,
                "credibility": credibility,
                "relevance": relevance,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if ratings is not UNSET:
            field_dict["ratings"] = ratings
        if citations is not UNSET:
            field_dict["citations"] = citations

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ach_evidence_input_ratings import AchEvidenceInputRatings

        d = dict(src_dict)
        source = AchEvidenceInputSource(d.pop("source"))

        description = d.pop("description")

        credibility = AchEvidenceInputCredibility(d.pop("credibility"))

        relevance = AchEvidenceInputRelevance(d.pop("relevance"))

        def _parse_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ref = _parse_ref(d.pop("ref", UNSET))

        _ratings = d.pop("ratings", UNSET)
        ratings: AchEvidenceInputRatings | Unset
        if isinstance(_ratings, Unset):
            ratings = UNSET
        else:
            ratings = AchEvidenceInputRatings.from_dict(_ratings)

        citations = cast(list[str], d.pop("citations", UNSET))

        ach_evidence_input = cls(
            source=source,
            description=description,
            credibility=credibility,
            relevance=relevance,
            ref=ref,
            ratings=ratings,
            citations=citations,
        )

        ach_evidence_input.additional_properties = d
        return ach_evidence_input

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
