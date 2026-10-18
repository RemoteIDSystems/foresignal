from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.alert import Alert
    from ..models.digest_alerts_by_rule_item import DigestAlertsByRuleItem
    from ..models.digest_alerts_by_severity import DigestAlertsBySeverity
    from ..models.digest_alerts_by_status import DigestAlertsByStatus


T = TypeVar("T", bound="DigestAlerts")


@_attrs_define
class DigestAlerts:
    """Alerts in scope for a digest period, keyed by alert id (029)

    Attributes:
        since_alert_id (int | None | Unset):
        until_alert_id (int | None | Unset):
        by_severity (DigestAlertsBySeverity | Unset): Counts per severity
        by_status (DigestAlertsByStatus | Unset): Counts per lifecycle state (017)
        by_rule (list[DigestAlertsByRuleItem] | Unset):
        items (list[Alert] | Unset): Critical first
    """

    since_alert_id: int | None | Unset = UNSET
    until_alert_id: int | None | Unset = UNSET
    by_severity: DigestAlertsBySeverity | Unset = UNSET
    by_status: DigestAlertsByStatus | Unset = UNSET
    by_rule: list[DigestAlertsByRuleItem] | Unset = UNSET
    items: list[Alert] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        since_alert_id: int | None | Unset
        if isinstance(self.since_alert_id, Unset):
            since_alert_id = UNSET
        else:
            since_alert_id = self.since_alert_id

        until_alert_id: int | None | Unset
        if isinstance(self.until_alert_id, Unset):
            until_alert_id = UNSET
        else:
            until_alert_id = self.until_alert_id

        by_severity: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_severity, Unset):
            by_severity = self.by_severity.to_dict()

        by_status: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_status, Unset):
            by_status = self.by_status.to_dict()

        by_rule: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.by_rule, Unset):
            by_rule = []
            for by_rule_item_data in self.by_rule:
                by_rule_item = by_rule_item_data.to_dict()
                by_rule.append(by_rule_item)

        items: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.items, Unset):
            items = []
            for items_item_data in self.items:
                items_item = items_item_data.to_dict()
                items.append(items_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if since_alert_id is not UNSET:
            field_dict["sinceAlertId"] = since_alert_id
        if until_alert_id is not UNSET:
            field_dict["untilAlertId"] = until_alert_id
        if by_severity is not UNSET:
            field_dict["bySeverity"] = by_severity
        if by_status is not UNSET:
            field_dict["byStatus"] = by_status
        if by_rule is not UNSET:
            field_dict["byRule"] = by_rule
        if items is not UNSET:
            field_dict["items"] = items

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.alert import Alert
        from ..models.digest_alerts_by_rule_item import DigestAlertsByRuleItem
        from ..models.digest_alerts_by_severity import DigestAlertsBySeverity
        from ..models.digest_alerts_by_status import DigestAlertsByStatus

        d = dict(src_dict)

        def _parse_since_alert_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        since_alert_id = _parse_since_alert_id(d.pop("sinceAlertId", UNSET))

        def _parse_until_alert_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        until_alert_id = _parse_until_alert_id(d.pop("untilAlertId", UNSET))

        _by_severity = d.pop("bySeverity", UNSET)
        by_severity: DigestAlertsBySeverity | Unset
        if isinstance(_by_severity, Unset):
            by_severity = UNSET
        else:
            by_severity = DigestAlertsBySeverity.from_dict(_by_severity)

        _by_status = d.pop("byStatus", UNSET)
        by_status: DigestAlertsByStatus | Unset
        if isinstance(_by_status, Unset):
            by_status = UNSET
        else:
            by_status = DigestAlertsByStatus.from_dict(_by_status)

        _by_rule = d.pop("byRule", UNSET)
        by_rule: list[DigestAlertsByRuleItem] | Unset = UNSET
        if _by_rule is not UNSET:
            by_rule = []
            for by_rule_item_data in _by_rule:
                by_rule_item = DigestAlertsByRuleItem.from_dict(by_rule_item_data)

                by_rule.append(by_rule_item)

        _items = d.pop("items", UNSET)
        items: list[Alert] | Unset = UNSET
        if _items is not UNSET:
            items = []
            for items_item_data in _items:
                items_item = Alert.from_dict(items_item_data)

                items.append(items_item)

        digest_alerts = cls(
            since_alert_id=since_alert_id,
            until_alert_id=until_alert_id,
            by_severity=by_severity,
            by_status=by_status,
            by_rule=by_rule,
            items=items,
        )

        digest_alerts.additional_properties = d
        return digest_alerts

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
