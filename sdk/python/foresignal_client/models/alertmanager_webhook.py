from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.alertmanager_webhook_status import AlertmanagerWebhookStatus
from ..models.alertmanager_webhook_version import AlertmanagerWebhookVersion
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.alertmanager_webhook_alerts_item import AlertmanagerWebhookAlertsItem
    from ..models.alertmanager_webhook_common_annotations import (
        AlertmanagerWebhookCommonAnnotations,
    )
    from ..models.alertmanager_webhook_common_labels import (
        AlertmanagerWebhookCommonLabels,
    )
    from ..models.alertmanager_webhook_group_labels import (
        AlertmanagerWebhookGroupLabels,
    )


T = TypeVar("T", bound="AlertmanagerWebhook")


@_attrs_define
class AlertmanagerWebhook:
    """Prometheus Alertmanager webhook payload (version 4) accepted by the `alertmanager` format (024)

    Attributes:
        version (AlertmanagerWebhookVersion):
        alerts (list[AlertmanagerWebhookAlertsItem]):
        group_key (str | Unset):
        truncated_alerts (int | Unset):
        status (AlertmanagerWebhookStatus | Unset):
        receiver (str | Unset):
        group_labels (AlertmanagerWebhookGroupLabels | Unset):
        common_labels (AlertmanagerWebhookCommonLabels | Unset):
        common_annotations (AlertmanagerWebhookCommonAnnotations | Unset):
        external_url (str | Unset):
    """

    version: AlertmanagerWebhookVersion
    alerts: list[AlertmanagerWebhookAlertsItem]
    group_key: str | Unset = UNSET
    truncated_alerts: int | Unset = UNSET
    status: AlertmanagerWebhookStatus | Unset = UNSET
    receiver: str | Unset = UNSET
    group_labels: AlertmanagerWebhookGroupLabels | Unset = UNSET
    common_labels: AlertmanagerWebhookCommonLabels | Unset = UNSET
    common_annotations: AlertmanagerWebhookCommonAnnotations | Unset = UNSET
    external_url: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        version = self.version.value

        alerts = []
        for alerts_item_data in self.alerts:
            alerts_item = alerts_item_data.to_dict()
            alerts.append(alerts_item)

        group_key = self.group_key

        truncated_alerts = self.truncated_alerts

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        receiver = self.receiver

        group_labels: dict[str, Any] | Unset = UNSET
        if not isinstance(self.group_labels, Unset):
            group_labels = self.group_labels.to_dict()

        common_labels: dict[str, Any] | Unset = UNSET
        if not isinstance(self.common_labels, Unset):
            common_labels = self.common_labels.to_dict()

        common_annotations: dict[str, Any] | Unset = UNSET
        if not isinstance(self.common_annotations, Unset):
            common_annotations = self.common_annotations.to_dict()

        external_url = self.external_url

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "version": version,
                "alerts": alerts,
            }
        )
        if group_key is not UNSET:
            field_dict["groupKey"] = group_key
        if truncated_alerts is not UNSET:
            field_dict["truncatedAlerts"] = truncated_alerts
        if status is not UNSET:
            field_dict["status"] = status
        if receiver is not UNSET:
            field_dict["receiver"] = receiver
        if group_labels is not UNSET:
            field_dict["groupLabels"] = group_labels
        if common_labels is not UNSET:
            field_dict["commonLabels"] = common_labels
        if common_annotations is not UNSET:
            field_dict["commonAnnotations"] = common_annotations
        if external_url is not UNSET:
            field_dict["externalURL"] = external_url

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.alertmanager_webhook_alerts_item import (
            AlertmanagerWebhookAlertsItem,
        )
        from ..models.alertmanager_webhook_common_annotations import (
            AlertmanagerWebhookCommonAnnotations,
        )
        from ..models.alertmanager_webhook_common_labels import (
            AlertmanagerWebhookCommonLabels,
        )
        from ..models.alertmanager_webhook_group_labels import (
            AlertmanagerWebhookGroupLabels,
        )

        d = dict(src_dict)
        version = AlertmanagerWebhookVersion(d.pop("version"))

        alerts = []
        _alerts = d.pop("alerts")
        for alerts_item_data in _alerts:
            alerts_item = AlertmanagerWebhookAlertsItem.from_dict(alerts_item_data)

            alerts.append(alerts_item)

        group_key = d.pop("groupKey", UNSET)

        truncated_alerts = d.pop("truncatedAlerts", UNSET)

        _status = d.pop("status", UNSET)
        status: AlertmanagerWebhookStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = AlertmanagerWebhookStatus(_status)

        receiver = d.pop("receiver", UNSET)

        _group_labels = d.pop("groupLabels", UNSET)
        group_labels: AlertmanagerWebhookGroupLabels | Unset
        if isinstance(_group_labels, Unset):
            group_labels = UNSET
        else:
            group_labels = AlertmanagerWebhookGroupLabels.from_dict(_group_labels)

        _common_labels = d.pop("commonLabels", UNSET)
        common_labels: AlertmanagerWebhookCommonLabels | Unset
        if isinstance(_common_labels, Unset):
            common_labels = UNSET
        else:
            common_labels = AlertmanagerWebhookCommonLabels.from_dict(_common_labels)

        _common_annotations = d.pop("commonAnnotations", UNSET)
        common_annotations: AlertmanagerWebhookCommonAnnotations | Unset
        if isinstance(_common_annotations, Unset):
            common_annotations = UNSET
        else:
            common_annotations = AlertmanagerWebhookCommonAnnotations.from_dict(
                _common_annotations
            )

        external_url = d.pop("externalURL", UNSET)

        alertmanager_webhook = cls(
            version=version,
            alerts=alerts,
            group_key=group_key,
            truncated_alerts=truncated_alerts,
            status=status,
            receiver=receiver,
            group_labels=group_labels,
            common_labels=common_labels,
            common_annotations=common_annotations,
            external_url=external_url,
        )

        alertmanager_webhook.additional_properties = d
        return alertmanager_webhook

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
