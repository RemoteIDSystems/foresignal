from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.severity import Severity
from ..types import UNSET, Unset

T = TypeVar("T", bound="BreakingNewsConfig")


@_attrs_define
class BreakingNewsConfig:
    """Off-schedule breaking-news editions (NP-006): an alert of one of `ruleIds` at or above `minSeverity` runs the
    edition at once, besides its schedule, at most once per `cooldownMinutes` (a burst coalesces into one run).

        Attributes:
            enabled (bool):  Default: False.
            rule_ids (list[UUID] | Unset): Trigger rules whose alerts fire a breaking edition
            min_severity (Severity | Unset):
            cooldown_minutes (int | Unset): The least time between two breaking runs of the edition Default: 60.
    """

    enabled: bool = False
    rule_ids: list[UUID] | Unset = UNSET
    min_severity: Severity | Unset = UNSET
    cooldown_minutes: int | Unset = 60
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        rule_ids: list[str] | Unset = UNSET
        if not isinstance(self.rule_ids, Unset):
            rule_ids = []
            for rule_ids_item_data in self.rule_ids:
                rule_ids_item = str(rule_ids_item_data)
                rule_ids.append(rule_ids_item)

        min_severity: str | Unset = UNSET
        if not isinstance(self.min_severity, Unset):
            min_severity = self.min_severity.value

        cooldown_minutes = self.cooldown_minutes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "enabled": enabled,
            }
        )
        if rule_ids is not UNSET:
            field_dict["ruleIds"] = rule_ids
        if min_severity is not UNSET:
            field_dict["minSeverity"] = min_severity
        if cooldown_minutes is not UNSET:
            field_dict["cooldownMinutes"] = cooldown_minutes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        enabled = d.pop("enabled")

        _rule_ids = d.pop("ruleIds", UNSET)
        rule_ids: list[UUID] | Unset = UNSET
        if _rule_ids is not UNSET:
            rule_ids = []
            for rule_ids_item_data in _rule_ids:
                rule_ids_item = UUID(rule_ids_item_data)

                rule_ids.append(rule_ids_item)

        _min_severity = d.pop("minSeverity", UNSET)
        min_severity: Severity | Unset
        if isinstance(_min_severity, Unset):
            min_severity = UNSET
        else:
            min_severity = Severity(_min_severity)

        cooldown_minutes = d.pop("cooldownMinutes", UNSET)

        breaking_news_config = cls(
            enabled=enabled,
            rule_ids=rule_ids,
            min_severity=min_severity,
            cooldown_minutes=cooldown_minutes,
        )

        breaking_news_config.additional_properties = d
        return breaking_news_config

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
