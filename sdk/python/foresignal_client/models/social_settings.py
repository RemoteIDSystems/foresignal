from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_settings_attention_gap import SocialSettingsAttentionGap
    from ..models.social_settings_compare import SocialSettingsCompare
    from ..models.social_settings_cues import SocialSettingsCues
    from ..models.social_settings_track_record import SocialSettingsTrackRecord
    from ..models.social_settings_velocity import SocialSettingsVelocity


T = TypeVar("T", bound="SocialSettings")


@_attrs_define
class SocialSettings:
    """Instance social settings (`social.*`): languages, coverage, velocity, claim extraction, track-record grading, media,
    comparisons, attention gaps and cues (135–137). Platform settings live on `/v1/social/platforms/{platformId}`

        Attributes:
            languages (list[str] | Unset): Detection languages (Lingua)
            min_region_baseline (float | Unset): Posts per hour below which social observability is LOW_SOCIAL_COVERAGE
                Default: 20.0.
            velocity (SocialSettingsVelocity | Unset): `social-velocity` finding thresholds
            llm_claim_min_posts (int | Unset):  Default: 50.
            llm_claim_min_evidence_groups (int | Unset): Evidence groups that make a narrative eligible for LLM claim
                extraction whatever its size; fixed at 5 in this release (a PUT with another value is refused) Default: 5.
            keep_media (bool | Unset): Store fingerprinted images in 043 under the post's retention Default: False.
            track_record (SocialSettingsTrackRecord | Unset): Admiralty source-reliability grading of accounts and channels
            compare (SocialSettingsCompare | Unset): Social–physical comparison
            attention_gap (SocialSettingsAttentionGap | Unset): Attention-gap detection
            cues (SocialSettingsCues | Unset): Automatic and manual cues
    """

    languages: list[str] | Unset = UNSET
    min_region_baseline: float | Unset = 20.0
    velocity: SocialSettingsVelocity | Unset = UNSET
    llm_claim_min_posts: int | Unset = 50
    llm_claim_min_evidence_groups: int | Unset = 5
    keep_media: bool | Unset = False
    track_record: SocialSettingsTrackRecord | Unset = UNSET
    compare: SocialSettingsCompare | Unset = UNSET
    attention_gap: SocialSettingsAttentionGap | Unset = UNSET
    cues: SocialSettingsCues | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        languages: list[str] | Unset = UNSET
        if not isinstance(self.languages, Unset):
            languages = self.languages

        min_region_baseline = self.min_region_baseline

        velocity: dict[str, Any] | Unset = UNSET
        if not isinstance(self.velocity, Unset):
            velocity = self.velocity.to_dict()

        llm_claim_min_posts = self.llm_claim_min_posts

        llm_claim_min_evidence_groups = self.llm_claim_min_evidence_groups

        keep_media = self.keep_media

        track_record: dict[str, Any] | Unset = UNSET
        if not isinstance(self.track_record, Unset):
            track_record = self.track_record.to_dict()

        compare: dict[str, Any] | Unset = UNSET
        if not isinstance(self.compare, Unset):
            compare = self.compare.to_dict()

        attention_gap: dict[str, Any] | Unset = UNSET
        if not isinstance(self.attention_gap, Unset):
            attention_gap = self.attention_gap.to_dict()

        cues: dict[str, Any] | Unset = UNSET
        if not isinstance(self.cues, Unset):
            cues = self.cues.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if languages is not UNSET:
            field_dict["languages"] = languages
        if min_region_baseline is not UNSET:
            field_dict["minRegionBaseline"] = min_region_baseline
        if velocity is not UNSET:
            field_dict["velocity"] = velocity
        if llm_claim_min_posts is not UNSET:
            field_dict["llmClaimMinPosts"] = llm_claim_min_posts
        if llm_claim_min_evidence_groups is not UNSET:
            field_dict["llmClaimMinEvidenceGroups"] = llm_claim_min_evidence_groups
        if keep_media is not UNSET:
            field_dict["keepMedia"] = keep_media
        if track_record is not UNSET:
            field_dict["trackRecord"] = track_record
        if compare is not UNSET:
            field_dict["compare"] = compare
        if attention_gap is not UNSET:
            field_dict["attentionGap"] = attention_gap
        if cues is not UNSET:
            field_dict["cues"] = cues

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_settings_attention_gap import SocialSettingsAttentionGap
        from ..models.social_settings_compare import SocialSettingsCompare
        from ..models.social_settings_cues import SocialSettingsCues
        from ..models.social_settings_track_record import SocialSettingsTrackRecord
        from ..models.social_settings_velocity import SocialSettingsVelocity

        d = dict(src_dict)
        languages = cast(list[str], d.pop("languages", UNSET))

        min_region_baseline = d.pop("minRegionBaseline", UNSET)

        _velocity = d.pop("velocity", UNSET)
        velocity: SocialSettingsVelocity | Unset
        if isinstance(_velocity, Unset):
            velocity = UNSET
        else:
            velocity = SocialSettingsVelocity.from_dict(_velocity)

        llm_claim_min_posts = d.pop("llmClaimMinPosts", UNSET)

        llm_claim_min_evidence_groups = d.pop("llmClaimMinEvidenceGroups", UNSET)

        keep_media = d.pop("keepMedia", UNSET)

        _track_record = d.pop("trackRecord", UNSET)
        track_record: SocialSettingsTrackRecord | Unset
        if isinstance(_track_record, Unset):
            track_record = UNSET
        else:
            track_record = SocialSettingsTrackRecord.from_dict(_track_record)

        _compare = d.pop("compare", UNSET)
        compare: SocialSettingsCompare | Unset
        if isinstance(_compare, Unset):
            compare = UNSET
        else:
            compare = SocialSettingsCompare.from_dict(_compare)

        _attention_gap = d.pop("attentionGap", UNSET)
        attention_gap: SocialSettingsAttentionGap | Unset
        if isinstance(_attention_gap, Unset):
            attention_gap = UNSET
        else:
            attention_gap = SocialSettingsAttentionGap.from_dict(_attention_gap)

        _cues = d.pop("cues", UNSET)
        cues: SocialSettingsCues | Unset
        if isinstance(_cues, Unset):
            cues = UNSET
        else:
            cues = SocialSettingsCues.from_dict(_cues)

        social_settings = cls(
            languages=languages,
            min_region_baseline=min_region_baseline,
            velocity=velocity,
            llm_claim_min_posts=llm_claim_min_posts,
            llm_claim_min_evidence_groups=llm_claim_min_evidence_groups,
            keep_media=keep_media,
            track_record=track_record,
            compare=compare,
            attention_gap=attention_gap,
            cues=cues,
        )

        social_settings.additional_properties = d
        return social_settings

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
