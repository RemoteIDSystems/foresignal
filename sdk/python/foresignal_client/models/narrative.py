from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.narrative_summary_state import NarrativeSummaryState
from ..models.narrative_summary_status import NarrativeSummaryStatus
from ..models.tlp import Tlp
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.claim import Claim
    from ..models.coordination_indicators import CoordinationIndicators
    from ..models.detector_ref import DetectorRef
    from ..models.media_fingerprint import MediaFingerprint
    from ..models.narrative_log_entry import NarrativeLogEntry
    from ..models.narrative_platform_stats import NarrativePlatformStats
    from ..models.narrative_summary_origin_concentration import (
        NarrativeSummaryOriginConcentration,
    )
    from ..models.object_envelope import ObjectEnvelope


T = TypeVar("T", bound="Narrative")


@_attrs_define
class Narrative:
    """Narrative detail: summary fields plus envelope, claims, media, coordination, linked stories and situations, and
    history (136)

        Attributes:
            id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
                `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
                `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
                situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            title (str): Deterministic: the most central member's text truncated to 140 chars, then the claim summary once 2
                or more claims exist
            canonical_event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            platforms (list[NarrativePlatformStats]):
            first_observed_at (datetime.datetime):
            last_activity_at (datetime.datetime):
            post_count (int):
            unique_authors (int):
            evidence_group_count (int): Independent origins (reposts, quotes, shared links and media join their original's
                group)
            origin_concentration (NarrativeSummaryOriginConcentration): Share of posts descending from the top three
                evidence groups
            status (NarrativeSummaryStatus): Set from 137 comparisons; describes the relationship to independent evidence,
                never truth
            state (NarrativeSummaryState): By velocity and age
            tlp (Tlp): Traffic Light Protocol 2.0 label
            envelope (ObjectEnvelope): Uniform summary of any Foresignal object (docs/Client-Contract.md §3)
            claims (list[Claim]):
            media (list[MediaFingerprint]):
            history (list[NarrativeLogEntry]):
            clusterer (DetectorRef):
            manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            topics (list[str] | Unset): 117 attention topic ids
            place_refs (list[str] | Unset):
            entity_refs (list[str] | Unset):
            region_codes (list[str] | Unset):
            languages (list[str] | Unset):
            velocity (float | None | Unset): Current hourly velocity ratio from 135 baselines
            media_fingerprints (list[str] | Unset): Media fingerprint ids
            news_story_ids (list[str] | Unset): 133 stories linked by URL or text
            situation_ids (list[str] | Unset):
            coordination_indicators_present (bool | Unset): A `social-coordination` finding exists for the narrative
            merged_into (None | str | Unset):
            coordination (CoordinationIndicators | None | Unset):
            news_stories (list[ObjectEnvelope] | Unset): Linked 133 stories
            situations (list[ObjectEnvelope] | Unset): Linked 118 situations
            comparison_ids (list[str] | Unset): 137 comparisons of the narrative's claims
            caveats (list[Caveat] | Unset):
    """

    id: str
    ref: str
    title: str
    canonical_event_id: str
    platforms: list[NarrativePlatformStats]
    first_observed_at: datetime.datetime
    last_activity_at: datetime.datetime
    post_count: int
    unique_authors: int
    evidence_group_count: int
    origin_concentration: NarrativeSummaryOriginConcentration
    status: NarrativeSummaryStatus
    state: NarrativeSummaryState
    tlp: Tlp
    envelope: ObjectEnvelope
    claims: list[Claim]
    media: list[MediaFingerprint]
    history: list[NarrativeLogEntry]
    clusterer: DetectorRef
    manifest_id: str
    topics: list[str] | Unset = UNSET
    place_refs: list[str] | Unset = UNSET
    entity_refs: list[str] | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    languages: list[str] | Unset = UNSET
    velocity: float | None | Unset = UNSET
    media_fingerprints: list[str] | Unset = UNSET
    news_story_ids: list[str] | Unset = UNSET
    situation_ids: list[str] | Unset = UNSET
    coordination_indicators_present: bool | Unset = UNSET
    merged_into: None | str | Unset = UNSET
    coordination: CoordinationIndicators | None | Unset = UNSET
    news_stories: list[ObjectEnvelope] | Unset = UNSET
    situations: list[ObjectEnvelope] | Unset = UNSET
    comparison_ids: list[str] | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.coordination_indicators import CoordinationIndicators

        id = self.id

        ref = self.ref

        title = self.title

        canonical_event_id = self.canonical_event_id

        platforms = []
        for platforms_item_data in self.platforms:
            platforms_item = platforms_item_data.to_dict()
            platforms.append(platforms_item)

        first_observed_at = self.first_observed_at.isoformat()

        last_activity_at = self.last_activity_at.isoformat()

        post_count = self.post_count

        unique_authors = self.unique_authors

        evidence_group_count = self.evidence_group_count

        origin_concentration = self.origin_concentration.to_dict()

        status = self.status.value

        state = self.state.value

        tlp = self.tlp.value

        envelope = self.envelope.to_dict()

        claims = []
        for claims_item_data in self.claims:
            claims_item = claims_item_data.to_dict()
            claims.append(claims_item)

        media = []
        for media_item_data in self.media:
            media_item = media_item_data.to_dict()
            media.append(media_item)

        history = []
        for history_item_data in self.history:
            history_item = history_item_data.to_dict()
            history.append(history_item)

        clusterer = self.clusterer.to_dict()

        manifest_id = self.manifest_id

        topics: list[str] | Unset = UNSET
        if not isinstance(self.topics, Unset):
            topics = self.topics

        place_refs: list[str] | Unset = UNSET
        if not isinstance(self.place_refs, Unset):
            place_refs = self.place_refs

        entity_refs: list[str] | Unset = UNSET
        if not isinstance(self.entity_refs, Unset):
            entity_refs = self.entity_refs

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        languages: list[str] | Unset = UNSET
        if not isinstance(self.languages, Unset):
            languages = self.languages

        velocity: float | None | Unset
        if isinstance(self.velocity, Unset):
            velocity = UNSET
        else:
            velocity = self.velocity

        media_fingerprints: list[str] | Unset = UNSET
        if not isinstance(self.media_fingerprints, Unset):
            media_fingerprints = self.media_fingerprints

        news_story_ids: list[str] | Unset = UNSET
        if not isinstance(self.news_story_ids, Unset):
            news_story_ids = self.news_story_ids

        situation_ids: list[str] | Unset = UNSET
        if not isinstance(self.situation_ids, Unset):
            situation_ids = self.situation_ids

        coordination_indicators_present = self.coordination_indicators_present

        merged_into: None | str | Unset
        if isinstance(self.merged_into, Unset):
            merged_into = UNSET
        else:
            merged_into = self.merged_into

        coordination: dict[str, Any] | None | Unset
        if isinstance(self.coordination, Unset):
            coordination = UNSET
        elif isinstance(self.coordination, CoordinationIndicators):
            coordination = self.coordination.to_dict()
        else:
            coordination = self.coordination

        news_stories: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.news_stories, Unset):
            news_stories = []
            for news_stories_item_data in self.news_stories:
                news_stories_item = news_stories_item_data.to_dict()
                news_stories.append(news_stories_item)

        situations: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.situations, Unset):
            situations = []
            for situations_item_data in self.situations:
                situations_item = situations_item_data.to_dict()
                situations.append(situations_item)

        comparison_ids: list[str] | Unset = UNSET
        if not isinstance(self.comparison_ids, Unset):
            comparison_ids = self.comparison_ids

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "ref": ref,
                "title": title,
                "canonicalEventId": canonical_event_id,
                "platforms": platforms,
                "firstObservedAt": first_observed_at,
                "lastActivityAt": last_activity_at,
                "postCount": post_count,
                "uniqueAuthors": unique_authors,
                "evidenceGroupCount": evidence_group_count,
                "originConcentration": origin_concentration,
                "status": status,
                "state": state,
                "tlp": tlp,
                "envelope": envelope,
                "claims": claims,
                "media": media,
                "history": history,
                "clusterer": clusterer,
                "manifestId": manifest_id,
            }
        )
        if topics is not UNSET:
            field_dict["topics"] = topics
        if place_refs is not UNSET:
            field_dict["placeRefs"] = place_refs
        if entity_refs is not UNSET:
            field_dict["entityRefs"] = entity_refs
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if languages is not UNSET:
            field_dict["languages"] = languages
        if velocity is not UNSET:
            field_dict["velocity"] = velocity
        if media_fingerprints is not UNSET:
            field_dict["mediaFingerprints"] = media_fingerprints
        if news_story_ids is not UNSET:
            field_dict["newsStoryIds"] = news_story_ids
        if situation_ids is not UNSET:
            field_dict["situationIds"] = situation_ids
        if coordination_indicators_present is not UNSET:
            field_dict["coordinationIndicatorsPresent"] = (
                coordination_indicators_present
            )
        if merged_into is not UNSET:
            field_dict["mergedInto"] = merged_into
        if coordination is not UNSET:
            field_dict["coordination"] = coordination
        if news_stories is not UNSET:
            field_dict["newsStories"] = news_stories
        if situations is not UNSET:
            field_dict["situations"] = situations
        if comparison_ids is not UNSET:
            field_dict["comparisonIds"] = comparison_ids
        if caveats is not UNSET:
            field_dict["caveats"] = caveats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.claim import Claim
        from ..models.coordination_indicators import CoordinationIndicators
        from ..models.detector_ref import DetectorRef
        from ..models.media_fingerprint import MediaFingerprint
        from ..models.narrative_log_entry import NarrativeLogEntry
        from ..models.narrative_platform_stats import NarrativePlatformStats
        from ..models.narrative_summary_origin_concentration import (
            NarrativeSummaryOriginConcentration,
        )
        from ..models.object_envelope import ObjectEnvelope

        d = dict(src_dict)
        id = d.pop("id")

        ref = d.pop("ref")

        title = d.pop("title")

        canonical_event_id = d.pop("canonicalEventId")

        platforms = []
        _platforms = d.pop("platforms")
        for platforms_item_data in _platforms:
            platforms_item = NarrativePlatformStats.from_dict(platforms_item_data)

            platforms.append(platforms_item)

        first_observed_at = datetime.datetime.fromisoformat(d.pop("firstObservedAt"))

        last_activity_at = datetime.datetime.fromisoformat(d.pop("lastActivityAt"))

        post_count = d.pop("postCount")

        unique_authors = d.pop("uniqueAuthors")

        evidence_group_count = d.pop("evidenceGroupCount")

        origin_concentration = NarrativeSummaryOriginConcentration.from_dict(
            d.pop("originConcentration")
        )

        status = NarrativeSummaryStatus(d.pop("status"))

        state = NarrativeSummaryState(d.pop("state"))

        tlp = Tlp(d.pop("tlp"))

        envelope = ObjectEnvelope.from_dict(d.pop("envelope"))

        claims = []
        _claims = d.pop("claims")
        for claims_item_data in _claims:
            claims_item = Claim.from_dict(claims_item_data)

            claims.append(claims_item)

        media = []
        _media = d.pop("media")
        for media_item_data in _media:
            media_item = MediaFingerprint.from_dict(media_item_data)

            media.append(media_item)

        history = []
        _history = d.pop("history")
        for history_item_data in _history:
            history_item = NarrativeLogEntry.from_dict(history_item_data)

            history.append(history_item)

        clusterer = DetectorRef.from_dict(d.pop("clusterer"))

        manifest_id = d.pop("manifestId")

        topics = cast(list[str], d.pop("topics", UNSET))

        place_refs = cast(list[str], d.pop("placeRefs", UNSET))

        entity_refs = cast(list[str], d.pop("entityRefs", UNSET))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        languages = cast(list[str], d.pop("languages", UNSET))

        def _parse_velocity(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        velocity = _parse_velocity(d.pop("velocity", UNSET))

        media_fingerprints = cast(list[str], d.pop("mediaFingerprints", UNSET))

        news_story_ids = cast(list[str], d.pop("newsStoryIds", UNSET))

        situation_ids = cast(list[str], d.pop("situationIds", UNSET))

        coordination_indicators_present = d.pop("coordinationIndicatorsPresent", UNSET)

        def _parse_merged_into(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        merged_into = _parse_merged_into(d.pop("mergedInto", UNSET))

        def _parse_coordination(data: object) -> CoordinationIndicators | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                coordination_type_0 = CoordinationIndicators.from_dict(data)

                return coordination_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(CoordinationIndicators | None | Unset, data)

        coordination = _parse_coordination(d.pop("coordination", UNSET))

        _news_stories = d.pop("newsStories", UNSET)
        news_stories: list[ObjectEnvelope] | Unset = UNSET
        if _news_stories is not UNSET:
            news_stories = []
            for news_stories_item_data in _news_stories:
                news_stories_item = ObjectEnvelope.from_dict(news_stories_item_data)

                news_stories.append(news_stories_item)

        _situations = d.pop("situations", UNSET)
        situations: list[ObjectEnvelope] | Unset = UNSET
        if _situations is not UNSET:
            situations = []
            for situations_item_data in _situations:
                situations_item = ObjectEnvelope.from_dict(situations_item_data)

                situations.append(situations_item)

        comparison_ids = cast(list[str], d.pop("comparisonIds", UNSET))

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        narrative = cls(
            id=id,
            ref=ref,
            title=title,
            canonical_event_id=canonical_event_id,
            platforms=platforms,
            first_observed_at=first_observed_at,
            last_activity_at=last_activity_at,
            post_count=post_count,
            unique_authors=unique_authors,
            evidence_group_count=evidence_group_count,
            origin_concentration=origin_concentration,
            status=status,
            state=state,
            tlp=tlp,
            envelope=envelope,
            claims=claims,
            media=media,
            history=history,
            clusterer=clusterer,
            manifest_id=manifest_id,
            topics=topics,
            place_refs=place_refs,
            entity_refs=entity_refs,
            region_codes=region_codes,
            languages=languages,
            velocity=velocity,
            media_fingerprints=media_fingerprints,
            news_story_ids=news_story_ids,
            situation_ids=situation_ids,
            coordination_indicators_present=coordination_indicators_present,
            merged_into=merged_into,
            coordination=coordination,
            news_stories=news_stories,
            situations=situations,
            comparison_ids=comparison_ids,
            caveats=caveats,
        )

        narrative.additional_properties = d
        return narrative

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
