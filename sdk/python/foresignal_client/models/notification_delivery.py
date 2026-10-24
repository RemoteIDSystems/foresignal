from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.notification_delivery_attachment_level_type_1 import (
    NotificationDeliveryAttachmentLevelType1,
)
from ..models.notification_delivery_attachment_level_type_2_type_1 import (
    NotificationDeliveryAttachmentLevelType2Type1,
)
from ..models.notification_delivery_attachment_level_type_3_type_1 import (
    NotificationDeliveryAttachmentLevelType3Type1,
)
from ..models.notification_delivery_deferred_reason_type_1 import (
    NotificationDeliveryDeferredReasonType1,
)
from ..models.notification_delivery_deferred_reason_type_2_type_1 import (
    NotificationDeliveryDeferredReasonType2Type1,
)
from ..models.notification_delivery_deferred_reason_type_3_type_1 import (
    NotificationDeliveryDeferredReasonType3Type1,
)
from ..models.notification_delivery_kind import NotificationDeliveryKind
from ..models.notification_delivery_status import NotificationDeliveryStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attachment import Attachment


T = TypeVar("T", bound="NotificationDelivery")


@_attrs_define
class NotificationDelivery:
    """
    Attributes:
        id (int):
        event (str): Expected to grow (`x-extensible-enum`): a client must accept a value not listed here.
        status (NotificationDeliveryStatus):
        attempts (int):
        alert_id (int | None | Unset):
        briefing_run_id (None | Unset | UUID):
        last_error (None | str | Unset):
        next_attempt_at (datetime.datetime | None | Unset):
        delivered_at (datetime.datetime | None | Unset):
        notification_id (int | Unset):
        kind (NotificationDeliveryKind | Unset):
        deferred_reason (None | NotificationDeliveryDeferredReasonType1 | NotificationDeliveryDeferredReasonType2Type1 |
            NotificationDeliveryDeferredReasonType3Type1 | Unset): `airtime` = held by a mesh channel's LoRa airtime
            governor (084); released as one digest line.
        edited (bool | Unset): An update delivered by editing the original message
        note (None | str | Unset): e.g. folded into delivery <id>
        escalation_step (int | None | Unset): Set on escalation deliveries
        attachment_level (None | NotificationDeliveryAttachmentLevelType1 |
            NotificationDeliveryAttachmentLevelType2Type1 | NotificationDeliveryAttachmentLevelType3Type1 | Unset): Size-
            control level chosen for this channel (800 px → 600 px → 256 px thumbnail → text only)
        attachments (list[Attachment] | Unset): Media references sent (bytes load from the media store at send time, so
            retries work)
        short_code (None | str | Unset): 4-character Crockford base32 alert code used by mesh channels so `#E ACK
            <code>` finds the alert (084)
    """

    id: int
    event: str
    status: NotificationDeliveryStatus
    attempts: int
    alert_id: int | None | Unset = UNSET
    briefing_run_id: None | Unset | UUID = UNSET
    last_error: None | str | Unset = UNSET
    next_attempt_at: datetime.datetime | None | Unset = UNSET
    delivered_at: datetime.datetime | None | Unset = UNSET
    notification_id: int | Unset = UNSET
    kind: NotificationDeliveryKind | Unset = UNSET
    deferred_reason: (
        None
        | NotificationDeliveryDeferredReasonType1
        | NotificationDeliveryDeferredReasonType2Type1
        | NotificationDeliveryDeferredReasonType3Type1
        | Unset
    ) = UNSET
    edited: bool | Unset = UNSET
    note: None | str | Unset = UNSET
    escalation_step: int | None | Unset = UNSET
    attachment_level: (
        None
        | NotificationDeliveryAttachmentLevelType1
        | NotificationDeliveryAttachmentLevelType2Type1
        | NotificationDeliveryAttachmentLevelType3Type1
        | Unset
    ) = UNSET
    attachments: list[Attachment] | Unset = UNSET
    short_code: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        event = self.event

        status = self.status.value

        attempts = self.attempts

        alert_id: int | None | Unset
        if isinstance(self.alert_id, Unset):
            alert_id = UNSET
        else:
            alert_id = self.alert_id

        briefing_run_id: None | str | Unset
        if isinstance(self.briefing_run_id, Unset):
            briefing_run_id = UNSET
        elif isinstance(self.briefing_run_id, UUID):
            briefing_run_id = str(self.briefing_run_id)
        else:
            briefing_run_id = self.briefing_run_id

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        next_attempt_at: None | str | Unset
        if isinstance(self.next_attempt_at, Unset):
            next_attempt_at = UNSET
        elif isinstance(self.next_attempt_at, datetime.datetime):
            next_attempt_at = self.next_attempt_at.isoformat()
        else:
            next_attempt_at = self.next_attempt_at

        delivered_at: None | str | Unset
        if isinstance(self.delivered_at, Unset):
            delivered_at = UNSET
        elif isinstance(self.delivered_at, datetime.datetime):
            delivered_at = self.delivered_at.isoformat()
        else:
            delivered_at = self.delivered_at

        notification_id = self.notification_id

        kind: str | Unset = UNSET
        if not isinstance(self.kind, Unset):
            kind = self.kind.value

        deferred_reason: None | str | Unset
        if isinstance(self.deferred_reason, Unset):
            deferred_reason = UNSET
        elif (
            isinstance(self.deferred_reason, NotificationDeliveryDeferredReasonType1)
            or isinstance(
                self.deferred_reason, NotificationDeliveryDeferredReasonType2Type1
            )
            or isinstance(
                self.deferred_reason, NotificationDeliveryDeferredReasonType3Type1
            )
        ):
            deferred_reason = self.deferred_reason.value
        else:
            deferred_reason = self.deferred_reason

        edited = self.edited

        note: None | str | Unset
        if isinstance(self.note, Unset):
            note = UNSET
        else:
            note = self.note

        escalation_step: int | None | Unset
        if isinstance(self.escalation_step, Unset):
            escalation_step = UNSET
        else:
            escalation_step = self.escalation_step

        attachment_level: None | str | Unset
        if isinstance(self.attachment_level, Unset):
            attachment_level = UNSET
        elif (
            isinstance(self.attachment_level, NotificationDeliveryAttachmentLevelType1)
            or isinstance(
                self.attachment_level, NotificationDeliveryAttachmentLevelType2Type1
            )
            or isinstance(
                self.attachment_level, NotificationDeliveryAttachmentLevelType3Type1
            )
        ):
            attachment_level = self.attachment_level.value
        else:
            attachment_level = self.attachment_level

        attachments: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.attachments, Unset):
            attachments = []
            for attachments_item_data in self.attachments:
                attachments_item = attachments_item_data.to_dict()
                attachments.append(attachments_item)

        short_code: None | str | Unset
        if isinstance(self.short_code, Unset):
            short_code = UNSET
        else:
            short_code = self.short_code

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "event": event,
                "status": status,
                "attempts": attempts,
            }
        )
        if alert_id is not UNSET:
            field_dict["alertId"] = alert_id
        if briefing_run_id is not UNSET:
            field_dict["briefingRunId"] = briefing_run_id
        if last_error is not UNSET:
            field_dict["lastError"] = last_error
        if next_attempt_at is not UNSET:
            field_dict["nextAttemptAt"] = next_attempt_at
        if delivered_at is not UNSET:
            field_dict["deliveredAt"] = delivered_at
        if notification_id is not UNSET:
            field_dict["notificationId"] = notification_id
        if kind is not UNSET:
            field_dict["kind"] = kind
        if deferred_reason is not UNSET:
            field_dict["deferredReason"] = deferred_reason
        if edited is not UNSET:
            field_dict["edited"] = edited
        if note is not UNSET:
            field_dict["note"] = note
        if escalation_step is not UNSET:
            field_dict["escalationStep"] = escalation_step
        if attachment_level is not UNSET:
            field_dict["attachmentLevel"] = attachment_level
        if attachments is not UNSET:
            field_dict["attachments"] = attachments
        if short_code is not UNSET:
            field_dict["shortCode"] = short_code

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attachment import Attachment

        d = dict(src_dict)
        id = d.pop("id")

        event = d.pop("event")

        status = NotificationDeliveryStatus(d.pop("status"))

        attempts = d.pop("attempts")

        def _parse_alert_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        alert_id = _parse_alert_id(d.pop("alertId", UNSET))

        def _parse_briefing_run_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                briefing_run_id_type_0 = UUID(data)

                return briefing_run_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        briefing_run_id = _parse_briefing_run_id(d.pop("briefingRunId", UNSET))

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        def _parse_next_attempt_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                next_attempt_at_type_0 = datetime.datetime.fromisoformat(data)

                return next_attempt_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        next_attempt_at = _parse_next_attempt_at(d.pop("nextAttemptAt", UNSET))

        def _parse_delivered_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                delivered_at_type_0 = datetime.datetime.fromisoformat(data)

                return delivered_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        delivered_at = _parse_delivered_at(d.pop("deliveredAt", UNSET))

        notification_id = d.pop("notificationId", UNSET)

        _kind = d.pop("kind", UNSET)
        kind: NotificationDeliveryKind | Unset
        if isinstance(_kind, Unset):
            kind = UNSET
        else:
            kind = NotificationDeliveryKind(_kind)

        def _parse_deferred_reason(
            data: object,
        ) -> (
            None
            | NotificationDeliveryDeferredReasonType1
            | NotificationDeliveryDeferredReasonType2Type1
            | NotificationDeliveryDeferredReasonType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                deferred_reason_type_1 = NotificationDeliveryDeferredReasonType1(data)

                return deferred_reason_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                deferred_reason_type_2_type_1 = (
                    NotificationDeliveryDeferredReasonType2Type1(data)
                )

                return deferred_reason_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                deferred_reason_type_3_type_1 = (
                    NotificationDeliveryDeferredReasonType3Type1(data)
                )

                return deferred_reason_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | NotificationDeliveryDeferredReasonType1
                | NotificationDeliveryDeferredReasonType2Type1
                | NotificationDeliveryDeferredReasonType3Type1
                | Unset,
                data,
            )

        deferred_reason = _parse_deferred_reason(d.pop("deferredReason", UNSET))

        edited = d.pop("edited", UNSET)

        def _parse_note(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        note = _parse_note(d.pop("note", UNSET))

        def _parse_escalation_step(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        escalation_step = _parse_escalation_step(d.pop("escalationStep", UNSET))

        def _parse_attachment_level(
            data: object,
        ) -> (
            None
            | NotificationDeliveryAttachmentLevelType1
            | NotificationDeliveryAttachmentLevelType2Type1
            | NotificationDeliveryAttachmentLevelType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                attachment_level_type_1 = NotificationDeliveryAttachmentLevelType1(data)

                return attachment_level_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                attachment_level_type_2_type_1 = (
                    NotificationDeliveryAttachmentLevelType2Type1(data)
                )

                return attachment_level_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                attachment_level_type_3_type_1 = (
                    NotificationDeliveryAttachmentLevelType3Type1(data)
                )

                return attachment_level_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | NotificationDeliveryAttachmentLevelType1
                | NotificationDeliveryAttachmentLevelType2Type1
                | NotificationDeliveryAttachmentLevelType3Type1
                | Unset,
                data,
            )

        attachment_level = _parse_attachment_level(d.pop("attachmentLevel", UNSET))

        _attachments = d.pop("attachments", UNSET)
        attachments: list[Attachment] | Unset = UNSET
        if _attachments is not UNSET:
            attachments = []
            for attachments_item_data in _attachments:
                attachments_item = Attachment.from_dict(attachments_item_data)

                attachments.append(attachments_item)

        def _parse_short_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        short_code = _parse_short_code(d.pop("shortCode", UNSET))

        notification_delivery = cls(
            id=id,
            event=event,
            status=status,
            attempts=attempts,
            alert_id=alert_id,
            briefing_run_id=briefing_run_id,
            last_error=last_error,
            next_attempt_at=next_attempt_at,
            delivered_at=delivered_at,
            notification_id=notification_id,
            kind=kind,
            deferred_reason=deferred_reason,
            edited=edited,
            note=note,
            escalation_step=escalation_step,
            attachment_level=attachment_level,
            attachments=attachments,
            short_code=short_code,
        )

        notification_delivery.additional_properties = d
        return notification_delivery

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
