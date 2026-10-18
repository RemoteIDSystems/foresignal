from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.severity import Severity
from ..models.situation_patch_status import SituationPatchStatus
from ..models.situation_theme import SituationTheme
from ..types import UNSET, Unset

T = TypeVar("T", bound="SituationPatch")


@_attrs_define
class SituationPatch:
    """JSON merge patch for a situation; every change is logged with the actor

    Attributes:
        title (str | Unset):
        theme (SituationTheme | Unset): Situation theme (118 `ThemeTable`)
        severity_override (None | Severity | Unset): Set to override the derived severity; null clears the override
        status (SituationPatchStatus | Unset): `active` activates or reopens a dormant situation; `closed` closes it
            (final)
        topic_ids (list[str] | Unset):
        entity_ids (list[str] | Unset):
        investigation_ids (list[str] | Unset):
        reason (None | str | Unset): Note stored in the situation log
        version (int | Unset): Expected current version; a mismatch returns 409 `SITUATION_VERSION_CONFLICT`
    """

    title: str | Unset = UNSET
    theme: SituationTheme | Unset = UNSET
    severity_override: None | Severity | Unset = UNSET
    status: SituationPatchStatus | Unset = UNSET
    topic_ids: list[str] | Unset = UNSET
    entity_ids: list[str] | Unset = UNSET
    investigation_ids: list[str] | Unset = UNSET
    reason: None | str | Unset = UNSET
    version: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        title = self.title

        theme: str | Unset = UNSET
        if not isinstance(self.theme, Unset):
            theme = self.theme.value

        severity_override: None | str | Unset
        if isinstance(self.severity_override, Unset):
            severity_override = UNSET
        elif isinstance(self.severity_override, Severity):
            severity_override = self.severity_override.value
        else:
            severity_override = self.severity_override

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        topic_ids: list[str] | Unset = UNSET
        if not isinstance(self.topic_ids, Unset):
            topic_ids = self.topic_ids

        entity_ids: list[str] | Unset = UNSET
        if not isinstance(self.entity_ids, Unset):
            entity_ids = self.entity_ids

        investigation_ids: list[str] | Unset = UNSET
        if not isinstance(self.investigation_ids, Unset):
            investigation_ids = self.investigation_ids

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        version = self.version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if title is not UNSET:
            field_dict["title"] = title
        if theme is not UNSET:
            field_dict["theme"] = theme
        if severity_override is not UNSET:
            field_dict["severityOverride"] = severity_override
        if status is not UNSET:
            field_dict["status"] = status
        if topic_ids is not UNSET:
            field_dict["topicIds"] = topic_ids
        if entity_ids is not UNSET:
            field_dict["entityIds"] = entity_ids
        if investigation_ids is not UNSET:
            field_dict["investigationIds"] = investigation_ids
        if reason is not UNSET:
            field_dict["reason"] = reason
        if version is not UNSET:
            field_dict["version"] = version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        title = d.pop("title", UNSET)

        _theme = d.pop("theme", UNSET)
        theme: SituationTheme | Unset
        if isinstance(_theme, Unset):
            theme = UNSET
        else:
            theme = SituationTheme(_theme)

        def _parse_severity_override(data: object) -> None | Severity | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                severity_override_type_0 = Severity(data)

                return severity_override_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Severity | Unset, data)

        severity_override = _parse_severity_override(d.pop("severityOverride", UNSET))

        _status = d.pop("status", UNSET)
        status: SituationPatchStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = SituationPatchStatus(_status)

        topic_ids = cast(list[str], d.pop("topicIds", UNSET))

        entity_ids = cast(list[str], d.pop("entityIds", UNSET))

        investigation_ids = cast(list[str], d.pop("investigationIds", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        version = d.pop("version", UNSET)

        situation_patch = cls(
            title=title,
            theme=theme,
            severity_override=severity_override,
            status=status,
            topic_ids=topic_ids,
            entity_ids=entity_ids,
            investigation_ids=investigation_ids,
            reason=reason,
            version=version,
        )

        situation_patch.additional_properties = d
        return situation_patch

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
