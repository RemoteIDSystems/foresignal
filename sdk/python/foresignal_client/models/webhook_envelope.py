from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.webhook_envelope_event import WebhookEnvelopeEvent
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.notification_message import NotificationMessage
    from ..models.webhook_envelope_data_type_0 import WebhookEnvelopeDataType0


T = TypeVar("T", bound="WebhookEnvelope")


@_attrs_define
class WebhookEnvelope:
    """Default body of the generic `webhook` channel (overridable by `bodyTemplate`)

    Attributes:
        event (WebhookEnvelopeEvent): Grows as the server adds events: a receiver must accept a value not listed here.
            Kept as `enum`, not `x-extensible-enum`, because this schema is also the `notification` webhook's payload, which
            the compatibility check cannot mark extensible.
        subtype (str):  Example: alert.fired.
        message (NotificationMessage): Persisted notification (one `notification` row) delivered to channels and the
            notification topics
        foresignal_origin (str): Install id of the sending instance
        data (None | Unset | WebhookEnvelopeDataType0):
        exercise (bool | Unset): Exercise delivery; also header X-Foresignal-Exercise: true Default: False.
    """

    event: WebhookEnvelopeEvent
    subtype: str
    message: NotificationMessage
    foresignal_origin: str
    data: None | Unset | WebhookEnvelopeDataType0 = UNSET
    exercise: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.webhook_envelope_data_type_0 import WebhookEnvelopeDataType0

        event = self.event.value

        subtype = self.subtype

        message = self.message.to_dict()

        foresignal_origin = self.foresignal_origin

        data: dict[str, Any] | None | Unset
        if isinstance(self.data, Unset):
            data = UNSET
        elif isinstance(self.data, WebhookEnvelopeDataType0):
            data = self.data.to_dict()
        else:
            data = self.data

        exercise = self.exercise

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "subtype": subtype,
                "message": message,
                "foresignalOrigin": foresignal_origin,
            }
        )
        if data is not UNSET:
            field_dict["data"] = data
        if exercise is not UNSET:
            field_dict["exercise"] = exercise

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.notification_message import NotificationMessage
        from ..models.webhook_envelope_data_type_0 import WebhookEnvelopeDataType0

        d = dict(src_dict)
        event = WebhookEnvelopeEvent(d.pop("event"))

        subtype = d.pop("subtype")

        message = NotificationMessage.from_dict(d.pop("message"))

        foresignal_origin = d.pop("foresignalOrigin")

        def _parse_data(data: object) -> None | Unset | WebhookEnvelopeDataType0:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                data_type_0 = WebhookEnvelopeDataType0.from_dict(data)

                return data_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | WebhookEnvelopeDataType0, data)

        data = _parse_data(d.pop("data", UNSET))

        exercise = d.pop("exercise", UNSET)

        webhook_envelope = cls(
            event=event,
            subtype=subtype,
            message=message,
            foresignal_origin=foresignal_origin,
            data=data,
            exercise=exercise,
        )

        webhook_envelope.additional_properties = d
        return webhook_envelope

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
