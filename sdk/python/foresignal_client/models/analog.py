from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.situation_theme import SituationTheme
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analog_outcomes import AnalogOutcomes
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="Analog")


@_attrs_define
class Analog:
    """A past situation (or region-period) similar to the target, with what happened next

    Attributes:
        title (str):
        region_codes (list[str]):
        period (TimeRange):
        rank (int):
        similarity (float): Reciprocal-rank-fused score
        outcomes (AnalogOutcomes): What happened next
        situation_id (None | str | Unset):
        theme (None | SituationTheme | Unset):
        feature_similarity (float | None | Unset): Feature cosine
        embedding_similarity (float | None | Unset): Summary embedding cosine (null without embeddings)
    """

    title: str
    region_codes: list[str]
    period: TimeRange
    rank: int
    similarity: float
    outcomes: AnalogOutcomes
    situation_id: None | str | Unset = UNSET
    theme: None | SituationTheme | Unset = UNSET
    feature_similarity: float | None | Unset = UNSET
    embedding_similarity: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        title = self.title

        region_codes = self.region_codes

        period = self.period.to_dict()

        rank = self.rank

        similarity = self.similarity

        outcomes = self.outcomes.to_dict()

        situation_id: None | str | Unset
        if isinstance(self.situation_id, Unset):
            situation_id = UNSET
        else:
            situation_id = self.situation_id

        theme: None | str | Unset
        if isinstance(self.theme, Unset):
            theme = UNSET
        elif isinstance(self.theme, SituationTheme):
            theme = self.theme.value
        else:
            theme = self.theme

        feature_similarity: float | None | Unset
        if isinstance(self.feature_similarity, Unset):
            feature_similarity = UNSET
        else:
            feature_similarity = self.feature_similarity

        embedding_similarity: float | None | Unset
        if isinstance(self.embedding_similarity, Unset):
            embedding_similarity = UNSET
        else:
            embedding_similarity = self.embedding_similarity

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "title": title,
                "regionCodes": region_codes,
                "period": period,
                "rank": rank,
                "similarity": similarity,
                "outcomes": outcomes,
            }
        )
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id
        if theme is not UNSET:
            field_dict["theme"] = theme
        if feature_similarity is not UNSET:
            field_dict["featureSimilarity"] = feature_similarity
        if embedding_similarity is not UNSET:
            field_dict["embeddingSimilarity"] = embedding_similarity

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analog_outcomes import AnalogOutcomes
        from ..models.time_range import TimeRange

        d = dict(src_dict)
        title = d.pop("title")

        region_codes = cast(list[str], d.pop("regionCodes"))

        period = TimeRange.from_dict(d.pop("period"))

        rank = d.pop("rank")

        similarity = d.pop("similarity")

        outcomes = AnalogOutcomes.from_dict(d.pop("outcomes"))

        def _parse_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        situation_id = _parse_situation_id(d.pop("situationId", UNSET))

        def _parse_theme(data: object) -> None | SituationTheme | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                theme_type_0 = SituationTheme(data)

                return theme_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SituationTheme | Unset, data)

        theme = _parse_theme(d.pop("theme", UNSET))

        def _parse_feature_similarity(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        feature_similarity = _parse_feature_similarity(
            d.pop("featureSimilarity", UNSET)
        )

        def _parse_embedding_similarity(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        embedding_similarity = _parse_embedding_similarity(
            d.pop("embeddingSimilarity", UNSET)
        )

        analog = cls(
            title=title,
            region_codes=region_codes,
            period=period,
            rank=rank,
            similarity=similarity,
            outcomes=outcomes,
            situation_id=situation_id,
            theme=theme,
            feature_similarity=feature_similarity,
            embedding_similarity=embedding_similarity,
        )

        analog.additional_properties = d
        return analog

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
