from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.exercise_stats_alerts_by_rule import ExerciseStatsAlertsByRule
    from ..models.exercise_stats_notifications_by_channel import (
        ExerciseStatsNotificationsByChannel,
    )
    from ..models.exercise_stats_skipped_rules_item import ExerciseStatsSkippedRulesItem


T = TypeVar("T", bound="ExerciseStats")


@_attrs_define
class ExerciseStats:
    """Statistics of an exercise run (projected for a dry run) (115)

    Attributes:
        versions_scanned (int):
        alerts_by_rule (ExerciseStatsAlertsByRule | Unset):
        notifications_by_channel (ExerciseStatsNotificationsByChannel | Unset):
        suppressed_by_cooldown (int | Unset):
        suppressed_by_rate_cap (int | Unset):
        skipped_rules (list[ExerciseStatsSkippedRulesItem] | Unset):
        caveats (list[Caveat] | Unset): e.g. PREDECESSOR_OUTSIDE_WINDOW
        alerts_raised (int | Unset): Exercise alerts actually raised (a canceled run stops early); absent for a dry run
        error (None | str | Unset): Why a FAILED run failed
    """

    versions_scanned: int
    alerts_by_rule: ExerciseStatsAlertsByRule | Unset = UNSET
    notifications_by_channel: ExerciseStatsNotificationsByChannel | Unset = UNSET
    suppressed_by_cooldown: int | Unset = UNSET
    suppressed_by_rate_cap: int | Unset = UNSET
    skipped_rules: list[ExerciseStatsSkippedRulesItem] | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    alerts_raised: int | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        versions_scanned = self.versions_scanned

        alerts_by_rule: dict[str, Any] | Unset = UNSET
        if not isinstance(self.alerts_by_rule, Unset):
            alerts_by_rule = self.alerts_by_rule.to_dict()

        notifications_by_channel: dict[str, Any] | Unset = UNSET
        if not isinstance(self.notifications_by_channel, Unset):
            notifications_by_channel = self.notifications_by_channel.to_dict()

        suppressed_by_cooldown = self.suppressed_by_cooldown

        suppressed_by_rate_cap = self.suppressed_by_rate_cap

        skipped_rules: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.skipped_rules, Unset):
            skipped_rules = []
            for skipped_rules_item_data in self.skipped_rules:
                skipped_rules_item = skipped_rules_item_data.to_dict()
                skipped_rules.append(skipped_rules_item)

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        alerts_raised = self.alerts_raised

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "versionsScanned": versions_scanned,
            }
        )
        if alerts_by_rule is not UNSET:
            field_dict["alertsByRule"] = alerts_by_rule
        if notifications_by_channel is not UNSET:
            field_dict["notificationsByChannel"] = notifications_by_channel
        if suppressed_by_cooldown is not UNSET:
            field_dict["suppressedByCooldown"] = suppressed_by_cooldown
        if suppressed_by_rate_cap is not UNSET:
            field_dict["suppressedByRateCap"] = suppressed_by_rate_cap
        if skipped_rules is not UNSET:
            field_dict["skippedRules"] = skipped_rules
        if caveats is not UNSET:
            field_dict["caveats"] = caveats
        if alerts_raised is not UNSET:
            field_dict["alertsRaised"] = alerts_raised
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.exercise_stats_alerts_by_rule import ExerciseStatsAlertsByRule
        from ..models.exercise_stats_notifications_by_channel import (
            ExerciseStatsNotificationsByChannel,
        )
        from ..models.exercise_stats_skipped_rules_item import (
            ExerciseStatsSkippedRulesItem,
        )

        d = dict(src_dict)
        versions_scanned = d.pop("versionsScanned")

        _alerts_by_rule = d.pop("alertsByRule", UNSET)
        alerts_by_rule: ExerciseStatsAlertsByRule | Unset
        if isinstance(_alerts_by_rule, Unset):
            alerts_by_rule = UNSET
        else:
            alerts_by_rule = ExerciseStatsAlertsByRule.from_dict(_alerts_by_rule)

        _notifications_by_channel = d.pop("notificationsByChannel", UNSET)
        notifications_by_channel: ExerciseStatsNotificationsByChannel | Unset
        if isinstance(_notifications_by_channel, Unset):
            notifications_by_channel = UNSET
        else:
            notifications_by_channel = ExerciseStatsNotificationsByChannel.from_dict(
                _notifications_by_channel
            )

        suppressed_by_cooldown = d.pop("suppressedByCooldown", UNSET)

        suppressed_by_rate_cap = d.pop("suppressedByRateCap", UNSET)

        _skipped_rules = d.pop("skippedRules", UNSET)
        skipped_rules: list[ExerciseStatsSkippedRulesItem] | Unset = UNSET
        if _skipped_rules is not UNSET:
            skipped_rules = []
            for skipped_rules_item_data in _skipped_rules:
                skipped_rules_item = ExerciseStatsSkippedRulesItem.from_dict(
                    skipped_rules_item_data
                )

                skipped_rules.append(skipped_rules_item)

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        alerts_raised = d.pop("alertsRaised", UNSET)

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        exercise_stats = cls(
            versions_scanned=versions_scanned,
            alerts_by_rule=alerts_by_rule,
            notifications_by_channel=notifications_by_channel,
            suppressed_by_cooldown=suppressed_by_cooldown,
            suppressed_by_rate_cap=suppressed_by_rate_cap,
            skipped_rules=skipped_rules,
            caveats=caveats,
            alerts_raised=alerts_raised,
            error=error,
        )

        exercise_stats.additional_properties = d
        return exercise_stats

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
