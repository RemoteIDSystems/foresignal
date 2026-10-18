from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.narrative_post_link_reason import NarrativePostLinkReason
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_post import SocialPost


T = TypeVar("T", bound="NarrativePost")


@_attrs_define
class NarrativePost:
    """A narrative member post with why it joined and its evidence group

    Attributes:
        post (SocialPost): A stored social post: record identity plus the `social-post` payload; edits are new versions
            (135)
        link_reason (NarrativePostLinkReason): `seed` for the first member
        evidence_group_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        joined_at (datetime.datetime):
        centrality (float | None | Unset): Similarity to the narrative centroid
    """

    post: SocialPost
    link_reason: NarrativePostLinkReason
    evidence_group_id: str
    joined_at: datetime.datetime
    centrality: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        post = self.post.to_dict()

        link_reason = self.link_reason.value

        evidence_group_id = self.evidence_group_id

        joined_at = self.joined_at.isoformat()

        centrality: float | None | Unset
        if isinstance(self.centrality, Unset):
            centrality = UNSET
        else:
            centrality = self.centrality

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "post": post,
                "linkReason": link_reason,
                "evidenceGroupId": evidence_group_id,
                "joinedAt": joined_at,
            }
        )
        if centrality is not UNSET:
            field_dict["centrality"] = centrality

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_post import SocialPost

        d = dict(src_dict)
        post = SocialPost.from_dict(d.pop("post"))

        link_reason = NarrativePostLinkReason(d.pop("linkReason"))

        evidence_group_id = d.pop("evidenceGroupId")

        joined_at = datetime.datetime.fromisoformat(d.pop("joinedAt"))

        def _parse_centrality(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        centrality = _parse_centrality(d.pop("centrality", UNSET))

        narrative_post = cls(
            post=post,
            link_reason=link_reason,
            evidence_group_id=evidence_group_id,
            joined_at=joined_at,
            centrality=centrality,
        )

        narrative_post.additional_properties = d
        return narrative_post

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
