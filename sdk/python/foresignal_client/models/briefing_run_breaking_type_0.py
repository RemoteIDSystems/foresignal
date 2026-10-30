from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.briefing_run_breaking_type_0_severity import (
    BriefingRunBreakingType0Severity,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="BriefingRunBreakingType0")


@_attrs_define
class BriefingRunBreakingType0:
    """The alert that fired this run as a breaking-news special edition (NP-006); null for a scheduled, manual or preview
    run. A breaking run is otherwise an ordinary run of the edition: same scope, output, delivery targets and site

        Attributes:
            alert_id (int):
            severity (BriefingRunBreakingType0Severity):
            rule_id (None | Unset | UUID):
            rule_name (None | str | Unset):
            fired_at (datetime.datetime | None | Unset):
            message (None | str | Unset):
    """

    alert_id: int
    severity: BriefingRunBreakingType0Severity
    rule_id: None | Unset | UUID = UNSET
    rule_name: None | str | Unset = UNSET
    fired_at: datetime.datetime | None | Unset = UNSET
    message: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        alert_id = self.alert_id

        severity = self.severity.value

        rule_id: None | str | Unset
        if isinstance(self.rule_id, Unset):
            rule_id = UNSET
        elif isinstance(self.rule_id, UUID):
            rule_id = str(self.rule_id)
        else:
            rule_id = self.rule_id

        rule_name: None | str | Unset
        if isinstance(self.rule_name, Unset):
            rule_name = UNSET
        else:
            rule_name = self.rule_name

        fired_at: None | str | Unset
        if isinstance(self.fired_at, Unset):
            fired_at = UNSET
        elif isinstance(self.fired_at, datetime.datetime):
            fired_at = self.fired_at.isoformat()
        else:
            fired_at = self.fired_at

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "alertId": alert_id,
                "severity": severity,
            }
        )
        if rule_id is not UNSET:
            field_dict["ruleId"] = rule_id
        if rule_name is not UNSET:
            field_dict["ruleName"] = rule_name
        if fired_at is not UNSET:
            field_dict["firedAt"] = fired_at
        if message is not UNSET:
            field_dict["message"] = message

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        alert_id = d.pop("alertId")

        severity = BriefingRunBreakingType0Severity(d.pop("severity"))

        def _parse_rule_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                rule_id_type_0 = UUID(data)

                return rule_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        rule_id = _parse_rule_id(d.pop("ruleId", UNSET))

        def _parse_rule_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        rule_name = _parse_rule_name(d.pop("ruleName", UNSET))

        def _parse_fired_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                fired_at_type_0 = datetime.datetime.fromisoformat(data)

                return fired_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        fired_at = _parse_fired_at(d.pop("firedAt", UNSET))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        briefing_run_breaking_type_0 = cls(
            alert_id=alert_id,
            severity=severity,
            rule_id=rule_id,
            rule_name=rule_name,
            fired_at=fired_at,
            message=message,
        )

        briefing_run_breaking_type_0.additional_properties = d
        return briefing_run_breaking_type_0

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
