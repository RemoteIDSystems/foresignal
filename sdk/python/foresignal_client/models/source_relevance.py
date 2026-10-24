from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.source_relevance_verdict import SourceRelevanceVerdict

T = TypeVar("T", bound="SourceRelevance")


@_attrs_define
class SourceRelevance:
    """One source's advisory verdict

    Attributes:
        source_id (str):
        name (str):
        kinds (list[str]): The kinds the source produces, sorted
        verdict (SourceRelevanceVerdict): relevant: belongs with the others; marginal: related but adds little or mostly
            noise; not-meaningful: unrelated to the rest; unrated: not judged (withheld by the egress policy, left out by
            the model, or the set could not be rated), with the reason
        reason (str): One sentence: the model's reason, or why the source was not rated
    """

    source_id: str
    name: str
    kinds: list[str]
    verdict: SourceRelevanceVerdict
    reason: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        name = self.name

        kinds = self.kinds

        verdict = self.verdict.value

        reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "name": name,
                "kinds": kinds,
                "verdict": verdict,
                "reason": reason,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        name = d.pop("name")

        kinds = cast(list[str], d.pop("kinds"))

        verdict = SourceRelevanceVerdict(d.pop("verdict"))

        reason = d.pop("reason")

        source_relevance = cls(
            source_id=source_id,
            name=name,
            kinds=kinds,
            verdict=verdict,
            reason=reason,
        )

        source_relevance.additional_properties = d
        return source_relevance

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
