from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.severity import Severity
from ..models.situation_input_status import SituationInputStatus
from ..models.situation_theme import SituationTheme
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.situation_member_input import SituationMemberInput


T = TypeVar("T", bound="SituationInput")


@_attrs_define
class SituationInput:
    """Create a situation by hand

    Attributes:
        title (str):
        theme (SituationTheme): Situation theme (118 `ThemeTable`)
        status (SituationInputStatus | Unset): Initial status (operators may set `active` directly) Default:
            SituationInputStatus.EMERGING.
        severity_override (None | Severity | Unset):
        region_codes (list[str] | Unset):
        topic_ids (list[str] | Unset):
        entity_ids (list[str] | Unset):
        investigation_ids (list[str] | Unset):
        members (list[SituationMemberInput] | Unset):
    """

    title: str
    theme: SituationTheme
    status: SituationInputStatus | Unset = SituationInputStatus.EMERGING
    severity_override: None | Severity | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    topic_ids: list[str] | Unset = UNSET
    entity_ids: list[str] | Unset = UNSET
    investigation_ids: list[str] | Unset = UNSET
    members: list[SituationMemberInput] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        title = self.title

        theme = self.theme.value

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        severity_override: None | str | Unset
        if isinstance(self.severity_override, Unset):
            severity_override = UNSET
        elif isinstance(self.severity_override, Severity):
            severity_override = self.severity_override.value
        else:
            severity_override = self.severity_override

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        topic_ids: list[str] | Unset = UNSET
        if not isinstance(self.topic_ids, Unset):
            topic_ids = self.topic_ids

        entity_ids: list[str] | Unset = UNSET
        if not isinstance(self.entity_ids, Unset):
            entity_ids = self.entity_ids

        investigation_ids: list[str] | Unset = UNSET
        if not isinstance(self.investigation_ids, Unset):
            investigation_ids = self.investigation_ids

        members: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.members, Unset):
            members = []
            for members_item_data in self.members:
                members_item = members_item_data.to_dict()
                members.append(members_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "title": title,
                "theme": theme,
            }
        )
        if status is not UNSET:
            field_dict["status"] = status
        if severity_override is not UNSET:
            field_dict["severityOverride"] = severity_override
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if topic_ids is not UNSET:
            field_dict["topicIds"] = topic_ids
        if entity_ids is not UNSET:
            field_dict["entityIds"] = entity_ids
        if investigation_ids is not UNSET:
            field_dict["investigationIds"] = investigation_ids
        if members is not UNSET:
            field_dict["members"] = members

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_member_input import SituationMemberInput

        d = dict(src_dict)
        title = d.pop("title")

        theme = SituationTheme(d.pop("theme"))

        _status = d.pop("status", UNSET)
        status: SituationInputStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = SituationInputStatus(_status)

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

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        topic_ids = cast(list[str], d.pop("topicIds", UNSET))

        entity_ids = cast(list[str], d.pop("entityIds", UNSET))

        investigation_ids = cast(list[str], d.pop("investigationIds", UNSET))

        _members = d.pop("members", UNSET)
        members: list[SituationMemberInput] | Unset = UNSET
        if _members is not UNSET:
            members = []
            for members_item_data in _members:
                members_item = SituationMemberInput.from_dict(members_item_data)

                members.append(members_item)

        situation_input = cls(
            title=title,
            theme=theme,
            status=status,
            severity_override=severity_override,
            region_codes=region_codes,
            topic_ids=topic_ids,
            entity_ids=entity_ids,
            investigation_ids=investigation_ids,
            members=members,
        )

        situation_input.additional_properties = d
        return situation_input

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
