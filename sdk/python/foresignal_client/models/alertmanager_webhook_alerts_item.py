from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.alertmanager_webhook_alerts_item_status import (
    AlertmanagerWebhookAlertsItemStatus,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.alertmanager_webhook_alerts_item_annotations import (
        AlertmanagerWebhookAlertsItemAnnotations,
    )
    from ..models.alertmanager_webhook_alerts_item_labels import (
        AlertmanagerWebhookAlertsItemLabels,
    )


T = TypeVar("T", bound="AlertmanagerWebhookAlertsItem")


@_attrs_define
class AlertmanagerWebhookAlertsItem:
    """One Alertmanager alert → one record (externalId = fingerprint)

    Attributes:
        status (AlertmanagerWebhookAlertsItemStatus):
        labels (AlertmanagerWebhookAlertsItemLabels): `latitude`/`longitude` (or `lat`/`lon`) become a point geometry
        starts_at (datetime.datetime):
        fingerprint (str):
        annotations (AlertmanagerWebhookAlertsItemAnnotations | Unset):
        ends_at (datetime.datetime | Unset):
        generator_url (str | Unset):
    """

    status: AlertmanagerWebhookAlertsItemStatus
    labels: AlertmanagerWebhookAlertsItemLabels
    starts_at: datetime.datetime
    fingerprint: str
    annotations: AlertmanagerWebhookAlertsItemAnnotations | Unset = UNSET
    ends_at: datetime.datetime | Unset = UNSET
    generator_url: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        labels = self.labels.to_dict()

        starts_at = self.starts_at.isoformat()

        fingerprint = self.fingerprint

        annotations: dict[str, Any] | Unset = UNSET
        if not isinstance(self.annotations, Unset):
            annotations = self.annotations.to_dict()

        ends_at: str | Unset = UNSET
        if not isinstance(self.ends_at, Unset):
            ends_at = self.ends_at.isoformat()

        generator_url = self.generator_url

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
                "labels": labels,
                "startsAt": starts_at,
                "fingerprint": fingerprint,
            }
        )
        if annotations is not UNSET:
            field_dict["annotations"] = annotations
        if ends_at is not UNSET:
            field_dict["endsAt"] = ends_at
        if generator_url is not UNSET:
            field_dict["generatorURL"] = generator_url

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.alertmanager_webhook_alerts_item_annotations import (
            AlertmanagerWebhookAlertsItemAnnotations,
        )
        from ..models.alertmanager_webhook_alerts_item_labels import (
            AlertmanagerWebhookAlertsItemLabels,
        )

        d = dict(src_dict)
        status = AlertmanagerWebhookAlertsItemStatus(d.pop("status"))

        labels = AlertmanagerWebhookAlertsItemLabels.from_dict(d.pop("labels"))

        starts_at = datetime.datetime.fromisoformat(d.pop("startsAt"))

        fingerprint = d.pop("fingerprint")

        _annotations = d.pop("annotations", UNSET)
        annotations: AlertmanagerWebhookAlertsItemAnnotations | Unset
        if isinstance(_annotations, Unset):
            annotations = UNSET
        else:
            annotations = AlertmanagerWebhookAlertsItemAnnotations.from_dict(
                _annotations
            )

        _ends_at = d.pop("endsAt", UNSET)
        ends_at: datetime.datetime | Unset
        if isinstance(_ends_at, Unset):
            ends_at = UNSET
        else:
            ends_at = datetime.datetime.fromisoformat(_ends_at)

        generator_url = d.pop("generatorURL", UNSET)

        alertmanager_webhook_alerts_item = cls(
            status=status,
            labels=labels,
            starts_at=starts_at,
            fingerprint=fingerprint,
            annotations=annotations,
            ends_at=ends_at,
            generator_url=generator_url,
        )

        alertmanager_webhook_alerts_item.additional_properties = d
        return alertmanager_webhook_alerts_item

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
