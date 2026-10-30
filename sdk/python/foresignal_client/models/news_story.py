from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.news_story_blindspot_type_1 import NewsStoryBlindspotType1
from ..models.news_story_blindspot_type_2_type_1 import NewsStoryBlindspotType2Type1
from ..models.news_story_blindspot_type_3_type_1 import NewsStoryBlindspotType3Type1
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.coverage_stats import CoverageStats
    from ..models.news_story_outlets_item import NewsStoryOutletsItem
    from ..models.news_story_translation_provenance_type_0 import (
        NewsStoryTranslationProvenanceType0,
    )
    from ..models.object_envelope import ObjectEnvelope


T = TypeVar("T", bound="NewsStory")


@_attrs_define
class NewsStory:
    """A cluster of articles about one event (116 canonical event, family `news-story`) with coverage statistics

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        canonical_event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        title (str): Deterministic: the most central article's title
        first_seen_at (datetime.datetime):
        last_seen_at (datetime.datetime):
        article_count (int):
        evidence_group_count (int):
        coverage (CoverageStats): Coverage of a story, topic, situation or a hypothesis's news support (CoverageAnalyzer
            1.0.0); describes reporting, never truth
        corroborated (bool):
        outlet_count (int | Unset):
        topic_ids (list[str] | Unset):
        region_codes (list[str] | Unset):
        blindspot (NewsStoryBlindspotType1 | NewsStoryBlindspotType2Type1 | NewsStoryBlindspotType3Type1 | None |
            Unset):
        situation_ids (list[str] | Unset):
        outlets (list[NewsStoryOutletsItem] | Unset):
        situations (list[ObjectEnvelope] | Unset): Detail view
        canonical_events (list[ObjectEnvelope] | Unset): Detail view
        target_language (None | str | Unset): The language of the translated fields, or null when untranslated
        translated_title (None | str | Unset): `title` in `targetLanguage`; the original fields are never overwritten
            (NP-002)
        translated_excerpt (None | str | Unset): The excerpt (an article's `summary`) in `targetLanguage`; the original
            fields are never overwritten (NP-002)
        translation_provenance (NewsStoryTranslationProvenanceType0 | None | Unset): How the translation was made
    """

    id: str
    ref: str
    canonical_event_id: str
    title: str
    first_seen_at: datetime.datetime
    last_seen_at: datetime.datetime
    article_count: int
    evidence_group_count: int
    coverage: CoverageStats
    corroborated: bool
    outlet_count: int | Unset = UNSET
    topic_ids: list[str] | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    blindspot: (
        NewsStoryBlindspotType1
        | NewsStoryBlindspotType2Type1
        | NewsStoryBlindspotType3Type1
        | None
        | Unset
    ) = UNSET
    situation_ids: list[str] | Unset = UNSET
    outlets: list[NewsStoryOutletsItem] | Unset = UNSET
    situations: list[ObjectEnvelope] | Unset = UNSET
    canonical_events: list[ObjectEnvelope] | Unset = UNSET
    target_language: None | str | Unset = UNSET
    translated_title: None | str | Unset = UNSET
    translated_excerpt: None | str | Unset = UNSET
    translation_provenance: NewsStoryTranslationProvenanceType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.news_story_translation_provenance_type_0 import (
            NewsStoryTranslationProvenanceType0,
        )

        id = self.id

        ref = self.ref

        canonical_event_id = self.canonical_event_id

        title = self.title

        first_seen_at = self.first_seen_at.isoformat()

        last_seen_at = self.last_seen_at.isoformat()

        article_count = self.article_count

        evidence_group_count = self.evidence_group_count

        coverage = self.coverage.to_dict()

        corroborated = self.corroborated

        outlet_count = self.outlet_count

        topic_ids: list[str] | Unset = UNSET
        if not isinstance(self.topic_ids, Unset):
            topic_ids = self.topic_ids

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        blindspot: None | str | Unset
        if isinstance(self.blindspot, Unset):
            blindspot = UNSET
        elif (
            isinstance(self.blindspot, NewsStoryBlindspotType1)
            or isinstance(self.blindspot, NewsStoryBlindspotType2Type1)
            or isinstance(self.blindspot, NewsStoryBlindspotType3Type1)
        ):
            blindspot = self.blindspot.value
        else:
            blindspot = self.blindspot

        situation_ids: list[str] | Unset = UNSET
        if not isinstance(self.situation_ids, Unset):
            situation_ids = self.situation_ids

        outlets: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.outlets, Unset):
            outlets = []
            for outlets_item_data in self.outlets:
                outlets_item = outlets_item_data.to_dict()
                outlets.append(outlets_item)

        situations: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.situations, Unset):
            situations = []
            for situations_item_data in self.situations:
                situations_item = situations_item_data.to_dict()
                situations.append(situations_item)

        canonical_events: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.canonical_events, Unset):
            canonical_events = []
            for canonical_events_item_data in self.canonical_events:
                canonical_events_item = canonical_events_item_data.to_dict()
                canonical_events.append(canonical_events_item)

        target_language: None | str | Unset
        if isinstance(self.target_language, Unset):
            target_language = UNSET
        else:
            target_language = self.target_language

        translated_title: None | str | Unset
        if isinstance(self.translated_title, Unset):
            translated_title = UNSET
        else:
            translated_title = self.translated_title

        translated_excerpt: None | str | Unset
        if isinstance(self.translated_excerpt, Unset):
            translated_excerpt = UNSET
        else:
            translated_excerpt = self.translated_excerpt

        translation_provenance: dict[str, Any] | None | Unset
        if isinstance(self.translation_provenance, Unset):
            translation_provenance = UNSET
        elif isinstance(
            self.translation_provenance, NewsStoryTranslationProvenanceType0
        ):
            translation_provenance = self.translation_provenance.to_dict()
        else:
            translation_provenance = self.translation_provenance

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "ref": ref,
                "canonicalEventId": canonical_event_id,
                "title": title,
                "firstSeenAt": first_seen_at,
                "lastSeenAt": last_seen_at,
                "articleCount": article_count,
                "evidenceGroupCount": evidence_group_count,
                "coverage": coverage,
                "corroborated": corroborated,
            }
        )
        if outlet_count is not UNSET:
            field_dict["outletCount"] = outlet_count
        if topic_ids is not UNSET:
            field_dict["topicIds"] = topic_ids
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if blindspot is not UNSET:
            field_dict["blindspot"] = blindspot
        if situation_ids is not UNSET:
            field_dict["situationIds"] = situation_ids
        if outlets is not UNSET:
            field_dict["outlets"] = outlets
        if situations is not UNSET:
            field_dict["situations"] = situations
        if canonical_events is not UNSET:
            field_dict["canonicalEvents"] = canonical_events
        if target_language is not UNSET:
            field_dict["targetLanguage"] = target_language
        if translated_title is not UNSET:
            field_dict["translatedTitle"] = translated_title
        if translated_excerpt is not UNSET:
            field_dict["translatedExcerpt"] = translated_excerpt
        if translation_provenance is not UNSET:
            field_dict["translationProvenance"] = translation_provenance

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.coverage_stats import CoverageStats
        from ..models.news_story_outlets_item import NewsStoryOutletsItem
        from ..models.news_story_translation_provenance_type_0 import (
            NewsStoryTranslationProvenanceType0,
        )
        from ..models.object_envelope import ObjectEnvelope

        d = dict(src_dict)
        id = d.pop("id")

        ref = d.pop("ref")

        canonical_event_id = d.pop("canonicalEventId")

        title = d.pop("title")

        first_seen_at = datetime.datetime.fromisoformat(d.pop("firstSeenAt"))

        last_seen_at = datetime.datetime.fromisoformat(d.pop("lastSeenAt"))

        article_count = d.pop("articleCount")

        evidence_group_count = d.pop("evidenceGroupCount")

        coverage = CoverageStats.from_dict(d.pop("coverage"))

        corroborated = d.pop("corroborated")

        outlet_count = d.pop("outletCount", UNSET)

        topic_ids = cast(list[str], d.pop("topicIds", UNSET))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        def _parse_blindspot(
            data: object,
        ) -> (
            NewsStoryBlindspotType1
            | NewsStoryBlindspotType2Type1
            | NewsStoryBlindspotType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                blindspot_type_1 = NewsStoryBlindspotType1(data)

                return blindspot_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                blindspot_type_2_type_1 = NewsStoryBlindspotType2Type1(data)

                return blindspot_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                blindspot_type_3_type_1 = NewsStoryBlindspotType3Type1(data)

                return blindspot_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                NewsStoryBlindspotType1
                | NewsStoryBlindspotType2Type1
                | NewsStoryBlindspotType3Type1
                | None
                | Unset,
                data,
            )

        blindspot = _parse_blindspot(d.pop("blindspot", UNSET))

        situation_ids = cast(list[str], d.pop("situationIds", UNSET))

        _outlets = d.pop("outlets", UNSET)
        outlets: list[NewsStoryOutletsItem] | Unset = UNSET
        if _outlets is not UNSET:
            outlets = []
            for outlets_item_data in _outlets:
                outlets_item = NewsStoryOutletsItem.from_dict(outlets_item_data)

                outlets.append(outlets_item)

        _situations = d.pop("situations", UNSET)
        situations: list[ObjectEnvelope] | Unset = UNSET
        if _situations is not UNSET:
            situations = []
            for situations_item_data in _situations:
                situations_item = ObjectEnvelope.from_dict(situations_item_data)

                situations.append(situations_item)

        _canonical_events = d.pop("canonicalEvents", UNSET)
        canonical_events: list[ObjectEnvelope] | Unset = UNSET
        if _canonical_events is not UNSET:
            canonical_events = []
            for canonical_events_item_data in _canonical_events:
                canonical_events_item = ObjectEnvelope.from_dict(
                    canonical_events_item_data
                )

                canonical_events.append(canonical_events_item)

        def _parse_target_language(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        target_language = _parse_target_language(d.pop("targetLanguage", UNSET))

        def _parse_translated_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        translated_title = _parse_translated_title(d.pop("translatedTitle", UNSET))

        def _parse_translated_excerpt(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        translated_excerpt = _parse_translated_excerpt(
            d.pop("translatedExcerpt", UNSET)
        )

        def _parse_translation_provenance(
            data: object,
        ) -> NewsStoryTranslationProvenanceType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                translation_provenance_type_0 = (
                    NewsStoryTranslationProvenanceType0.from_dict(data)
                )

                return translation_provenance_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(NewsStoryTranslationProvenanceType0 | None | Unset, data)

        translation_provenance = _parse_translation_provenance(
            d.pop("translationProvenance", UNSET)
        )

        news_story = cls(
            id=id,
            ref=ref,
            canonical_event_id=canonical_event_id,
            title=title,
            first_seen_at=first_seen_at,
            last_seen_at=last_seen_at,
            article_count=article_count,
            evidence_group_count=evidence_group_count,
            coverage=coverage,
            corroborated=corroborated,
            outlet_count=outlet_count,
            topic_ids=topic_ids,
            region_codes=region_codes,
            blindspot=blindspot,
            situation_ids=situation_ids,
            outlets=outlets,
            situations=situations,
            canonical_events=canonical_events,
            target_language=target_language,
            translated_title=translated_title,
            translated_excerpt=translated_excerpt,
            translation_provenance=translation_provenance,
        )

        news_story.additional_properties = d
        return news_story

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
