from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.media_fingerprint_uses_item import MediaFingerprintUsesItem
    from ..models.object_envelope import ObjectEnvelope


T = TypeVar("T", bound="MediaFingerprint")


@_attrs_define
class MediaFingerprint:
    """A PDQ fingerprint of an image or video thumbnail seen in posts (`MediaFingerprinter` 1.0.0); the image itself is
    discarded unless `social.keepMedia` (136)

        Attributes:
            id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            pdq (str): 256-bit PDQ hash, hex
            quality (int): PDQ quality; below 50 kept but not matched
            matchable (bool):
            first_seen_at (datetime.datetime):
            first_post_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
                `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
                `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
                situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            use_count (int):
            platforms (list[str]):
            narrative_ids (list[str]):
            media_id (None | str | Unset): 043 stored content when `social.keepMedia` is on
            reuse_finding_ids (list[str] | Unset): `media-reuse` findings
            uses (list[MediaFingerprintUsesItem] | Unset):
            narratives (list[ObjectEnvelope] | Unset): Detail view
    """

    id: str
    pdq: str
    quality: int
    matchable: bool
    first_seen_at: datetime.datetime
    first_post_ref: str
    use_count: int
    platforms: list[str]
    narrative_ids: list[str]
    media_id: None | str | Unset = UNSET
    reuse_finding_ids: list[str] | Unset = UNSET
    uses: list[MediaFingerprintUsesItem] | Unset = UNSET
    narratives: list[ObjectEnvelope] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        pdq = self.pdq

        quality = self.quality

        matchable = self.matchable

        first_seen_at = self.first_seen_at.isoformat()

        first_post_ref = self.first_post_ref

        use_count = self.use_count

        platforms = self.platforms

        narrative_ids = self.narrative_ids

        media_id: None | str | Unset
        if isinstance(self.media_id, Unset):
            media_id = UNSET
        else:
            media_id = self.media_id

        reuse_finding_ids: list[str] | Unset = UNSET
        if not isinstance(self.reuse_finding_ids, Unset):
            reuse_finding_ids = self.reuse_finding_ids

        uses: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.uses, Unset):
            uses = []
            for uses_item_data in self.uses:
                uses_item = uses_item_data.to_dict()
                uses.append(uses_item)

        narratives: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.narratives, Unset):
            narratives = []
            for narratives_item_data in self.narratives:
                narratives_item = narratives_item_data.to_dict()
                narratives.append(narratives_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "pdq": pdq,
                "quality": quality,
                "matchable": matchable,
                "firstSeenAt": first_seen_at,
                "firstPostRef": first_post_ref,
                "useCount": use_count,
                "platforms": platforms,
                "narrativeIds": narrative_ids,
            }
        )
        if media_id is not UNSET:
            field_dict["mediaId"] = media_id
        if reuse_finding_ids is not UNSET:
            field_dict["reuseFindingIds"] = reuse_finding_ids
        if uses is not UNSET:
            field_dict["uses"] = uses
        if narratives is not UNSET:
            field_dict["narratives"] = narratives

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.media_fingerprint_uses_item import MediaFingerprintUsesItem
        from ..models.object_envelope import ObjectEnvelope

        d = dict(src_dict)
        id = d.pop("id")

        pdq = d.pop("pdq")

        quality = d.pop("quality")

        matchable = d.pop("matchable")

        first_seen_at = datetime.datetime.fromisoformat(d.pop("firstSeenAt"))

        first_post_ref = d.pop("firstPostRef")

        use_count = d.pop("useCount")

        platforms = cast(list[str], d.pop("platforms"))

        narrative_ids = cast(list[str], d.pop("narrativeIds"))

        def _parse_media_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        media_id = _parse_media_id(d.pop("mediaId", UNSET))

        reuse_finding_ids = cast(list[str], d.pop("reuseFindingIds", UNSET))

        _uses = d.pop("uses", UNSET)
        uses: list[MediaFingerprintUsesItem] | Unset = UNSET
        if _uses is not UNSET:
            uses = []
            for uses_item_data in _uses:
                uses_item = MediaFingerprintUsesItem.from_dict(uses_item_data)

                uses.append(uses_item)

        _narratives = d.pop("narratives", UNSET)
        narratives: list[ObjectEnvelope] | Unset = UNSET
        if _narratives is not UNSET:
            narratives = []
            for narratives_item_data in _narratives:
                narratives_item = ObjectEnvelope.from_dict(narratives_item_data)

                narratives.append(narratives_item)

        media_fingerprint = cls(
            id=id,
            pdq=pdq,
            quality=quality,
            matchable=matchable,
            first_seen_at=first_seen_at,
            first_post_ref=first_post_ref,
            use_count=use_count,
            platforms=platforms,
            narrative_ids=narrative_ids,
            media_id=media_id,
            reuse_finding_ids=reuse_finding_ids,
            uses=uses,
            narratives=narratives,
        )

        media_fingerprint.additional_properties = d
        return media_fingerprint

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
