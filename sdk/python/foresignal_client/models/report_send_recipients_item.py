from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.report_send_recipients_item_status import ReportSendRecipientsItemStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="ReportSendRecipientsItem")


@_attrs_define
class ReportSendRecipientsItem:
    """Per-recipient outcome

    Attributes:
        grantee (str): `principal:{id}` or `group:{id}`
        status (ReportSendRecipientsItemStatus):
        channel_ids (list[str] | Unset):
    """

    grantee: str
    status: ReportSendRecipientsItemStatus
    channel_ids: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        grantee = self.grantee

        status = self.status.value

        channel_ids: list[str] | Unset = UNSET
        if not isinstance(self.channel_ids, Unset):
            channel_ids = self.channel_ids

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "grantee": grantee,
                "status": status,
            }
        )
        if channel_ids is not UNSET:
            field_dict["channelIds"] = channel_ids

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        grantee = d.pop("grantee")

        status = ReportSendRecipientsItemStatus(d.pop("status"))

        channel_ids = cast(list[str], d.pop("channelIds", UNSET))

        report_send_recipients_item = cls(
            grantee=grantee,
            status=status,
            channel_ids=channel_ids,
        )

        report_send_recipients_item.additional_properties = d
        return report_send_recipients_item

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
