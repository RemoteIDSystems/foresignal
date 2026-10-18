from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.principal_summary import PrincipalSummary
    from ..models.report_send_recipients_item import ReportSendRecipientsItem


T = TypeVar("T", bound="ReportSend")


@_attrs_define
class ReportSend:
    """A recorded send: viewer grants, notifications and per-recipient TLP checks

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        report_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        sent_by (PrincipalSummary): Public view of a principal: handle, display name, kind and avatar only
        sent_at (datetime.datetime):
        recipients (list[ReportSendRecipientsItem]):
        revision (int | None | Unset):
        message (None | str | Unset):
    """

    id: str
    report_id: str
    sent_by: PrincipalSummary
    sent_at: datetime.datetime
    recipients: list[ReportSendRecipientsItem]
    revision: int | None | Unset = UNSET
    message: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        report_id = self.report_id

        sent_by = self.sent_by.to_dict()

        sent_at = self.sent_at.isoformat()

        recipients = []
        for recipients_item_data in self.recipients:
            recipients_item = recipients_item_data.to_dict()
            recipients.append(recipients_item)

        revision: int | None | Unset
        if isinstance(self.revision, Unset):
            revision = UNSET
        else:
            revision = self.revision

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "reportId": report_id,
                "sentBy": sent_by,
                "sentAt": sent_at,
                "recipients": recipients,
            }
        )
        if revision is not UNSET:
            field_dict["revision"] = revision
        if message is not UNSET:
            field_dict["message"] = message

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.principal_summary import PrincipalSummary
        from ..models.report_send_recipients_item import ReportSendRecipientsItem

        d = dict(src_dict)
        id = d.pop("id")

        report_id = d.pop("reportId")

        sent_by = PrincipalSummary.from_dict(d.pop("sentBy"))

        sent_at = datetime.datetime.fromisoformat(d.pop("sentAt"))

        recipients = []
        _recipients = d.pop("recipients")
        for recipients_item_data in _recipients:
            recipients_item = ReportSendRecipientsItem.from_dict(recipients_item_data)

            recipients.append(recipients_item)

        def _parse_revision(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        revision = _parse_revision(d.pop("revision", UNSET))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        report_send = cls(
            id=id,
            report_id=report_id,
            sent_by=sent_by,
            sent_at=sent_at,
            recipients=recipients,
            revision=revision,
            message=message,
        )

        report_send.additional_properties = d
        return report_send

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
