from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.finding_severity import FindingSeverity
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_velocity_event_scope import SocialVelocityEventScope


T = TypeVar("T", bound="SocialVelocityEvent")


@_attrs_define
class SocialVelocityEvent:
    """SSE `social.velocity` (topic `social`): a `social-velocity` finding was created (135)

    Attributes:
        event (Literal['social.velocity']):
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        scope (SocialVelocityEventScope): Scope
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the
            platform an EDP provider (024, 138) declares Example: bluesky.
        velocity_ratio (float):
        unique_authors (int):
        severity (FindingSeverity):
        detected_at (datetime.datetime):
        language (None | str | Unset):
        z_robust (float | None | Unset):
    """

    event: Literal["social.velocity"]
    finding_id: str
    scope: SocialVelocityEventScope
    platform: str
    velocity_ratio: float
    unique_authors: int
    severity: FindingSeverity
    detected_at: datetime.datetime
    language: None | str | Unset = UNSET
    z_robust: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event

        finding_id = self.finding_id

        scope = self.scope.to_dict()

        platform = self.platform

        velocity_ratio = self.velocity_ratio

        unique_authors = self.unique_authors

        severity = self.severity.value

        detected_at = self.detected_at.isoformat()

        language: None | str | Unset
        if isinstance(self.language, Unset):
            language = UNSET
        else:
            language = self.language

        z_robust: float | None | Unset
        if isinstance(self.z_robust, Unset):
            z_robust = UNSET
        else:
            z_robust = self.z_robust

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "findingId": finding_id,
                "scope": scope,
                "platform": platform,
                "velocityRatio": velocity_ratio,
                "uniqueAuthors": unique_authors,
                "severity": severity,
                "detectedAt": detected_at,
            }
        )
        if language is not UNSET:
            field_dict["language"] = language
        if z_robust is not UNSET:
            field_dict["zRobust"] = z_robust

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_velocity_event_scope import SocialVelocityEventScope

        d = dict(src_dict)
        event = cast(Literal["social.velocity"], d.pop("event"))
        if event != "social.velocity":
            raise ValueError(f"event must match const 'social.velocity', got '{event}'")

        finding_id = d.pop("findingId")

        scope = SocialVelocityEventScope.from_dict(d.pop("scope"))

        platform = d.pop("platform")

        velocity_ratio = d.pop("velocityRatio")

        unique_authors = d.pop("uniqueAuthors")

        severity = FindingSeverity(d.pop("severity"))

        detected_at = datetime.datetime.fromisoformat(d.pop("detectedAt"))

        def _parse_language(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        language = _parse_language(d.pop("language", UNSET))

        def _parse_z_robust(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        z_robust = _parse_z_robust(d.pop("zRobust", UNSET))

        social_velocity_event = cls(
            event=event,
            finding_id=finding_id,
            scope=scope,
            platform=platform,
            velocity_ratio=velocity_ratio,
            unique_authors=unique_authors,
            severity=severity,
            detected_at=detected_at,
            language=language,
            z_robust=z_robust,
        )

        social_velocity_event.additional_properties = d
        return social_velocity_event

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
