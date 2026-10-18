from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.spc_outlook_payload_type import SpcOutlookPayloadType
from ..types import UNSET, Unset

T = TypeVar("T", bound="SpcOutlookPayload")


@_attrs_define
class SpcOutlookPayload:
    """Payload of `spc-outlook` records (externalId `day{d}:{type}:{LABEL}`)

    Attributes:
        day (int):
        type_ (SpcOutlookPayloadType):
        label (str):
        dn (int): Numeric rank for rules such as dn >= 5
        title (str):
        label2 (str | Unset):
        issue (datetime.datetime | Unset):
        valid (datetime.datetime | Unset):
        expire (datetime.datetime | Unset):
    """

    day: int
    type_: SpcOutlookPayloadType
    label: str
    dn: int
    title: str
    label2: str | Unset = UNSET
    issue: datetime.datetime | Unset = UNSET
    valid: datetime.datetime | Unset = UNSET
    expire: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        day = self.day

        type_ = self.type_.value

        label = self.label

        dn = self.dn

        title = self.title

        label2 = self.label2

        issue: str | Unset = UNSET
        if not isinstance(self.issue, Unset):
            issue = self.issue.isoformat()

        valid: str | Unset = UNSET
        if not isinstance(self.valid, Unset):
            valid = self.valid.isoformat()

        expire: str | Unset = UNSET
        if not isinstance(self.expire, Unset):
            expire = self.expire.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "day": day,
                "type": type_,
                "label": label,
                "dn": dn,
                "title": title,
            }
        )
        if label2 is not UNSET:
            field_dict["label2"] = label2
        if issue is not UNSET:
            field_dict["issue"] = issue
        if valid is not UNSET:
            field_dict["valid"] = valid
        if expire is not UNSET:
            field_dict["expire"] = expire

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        day = d.pop("day")

        type_ = SpcOutlookPayloadType(d.pop("type"))

        label = d.pop("label")

        dn = d.pop("dn")

        title = d.pop("title")

        label2 = d.pop("label2", UNSET)

        _issue = d.pop("issue", UNSET)
        issue: datetime.datetime | Unset
        if isinstance(_issue, Unset):
            issue = UNSET
        else:
            issue = datetime.datetime.fromisoformat(_issue)

        _valid = d.pop("valid", UNSET)
        valid: datetime.datetime | Unset
        if isinstance(_valid, Unset):
            valid = UNSET
        else:
            valid = datetime.datetime.fromisoformat(_valid)

        _expire = d.pop("expire", UNSET)
        expire: datetime.datetime | Unset
        if isinstance(_expire, Unset):
            expire = UNSET
        else:
            expire = datetime.datetime.fromisoformat(_expire)

        spc_outlook_payload = cls(
            day=day,
            type_=type_,
            label=label,
            dn=dn,
            title=title,
            label2=label2,
            issue=issue,
            valid=valid,
            expire=expire,
        )

        spc_outlook_payload.additional_properties = d
        return spc_outlook_payload

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
