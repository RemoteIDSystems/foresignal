from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.media_fingerprint import MediaFingerprint


T = TypeVar("T", bound="MediaMatch")


@_attrs_define
class MediaMatch:
    """A fingerprint within the match threshold of the query

    Attributes:
        fingerprint (MediaFingerprint): A PDQ fingerprint of an image or video thumbnail seen in posts
            (`MediaFingerprinter` 1.0.0); the image itself is discarded unless `social.keepMedia` (136)
        distance (int): Hamming distance
    """

    fingerprint: MediaFingerprint
    distance: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        fingerprint = self.fingerprint.to_dict()

        distance = self.distance

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "fingerprint": fingerprint,
                "distance": distance,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.media_fingerprint import MediaFingerprint

        d = dict(src_dict)
        fingerprint = MediaFingerprint.from_dict(d.pop("fingerprint"))

        distance = d.pop("distance")

        media_match = cls(
            fingerprint=fingerprint,
            distance=distance,
        )

        media_match.additional_properties = d
        return media_match

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
