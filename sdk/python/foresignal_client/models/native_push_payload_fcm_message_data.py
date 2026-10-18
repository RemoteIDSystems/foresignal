from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="NativePushPayloadFcmMessageData")


@_attrs_define
class NativePushPayloadFcmMessageData:
    """String-valued data

    Attributes:
        v (str | Unset):
        id (str | Unset):
        a (str | Unset):
        s (str | Unset):
        ev (str | Unset):
        l (str | Unset):
        t (str | Unset):
        c (str | Unset): Category FORESIGNAL_<EVENT> (032; actionable categories arrive in 033)
    """

    v: str | Unset = UNSET
    id: str | Unset = UNSET
    a: str | Unset = UNSET
    s: str | Unset = UNSET
    ev: str | Unset = UNSET
    l: str | Unset = UNSET
    t: str | Unset = UNSET
    c: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        v = self.v

        id = self.id

        a = self.a

        s = self.s

        ev = self.ev

        l = self.l

        t = self.t

        c = self.c

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if v is not UNSET:
            field_dict["v"] = v
        if id is not UNSET:
            field_dict["id"] = id
        if a is not UNSET:
            field_dict["a"] = a
        if s is not UNSET:
            field_dict["s"] = s
        if ev is not UNSET:
            field_dict["ev"] = ev
        if l is not UNSET:
            field_dict["l"] = l
        if t is not UNSET:
            field_dict["t"] = t
        if c is not UNSET:
            field_dict["c"] = c

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        v = d.pop("v", UNSET)

        id = d.pop("id", UNSET)

        a = d.pop("a", UNSET)

        s = d.pop("s", UNSET)

        ev = d.pop("ev", UNSET)

        l = d.pop("l", UNSET)

        t = d.pop("t", UNSET)

        c = d.pop("c", UNSET)

        native_push_payload_fcm_message_data = cls(
            v=v,
            id=id,
            a=a,
            s=s,
            ev=ev,
            l=l,
            t=t,
            c=c,
        )

        native_push_payload_fcm_message_data.additional_properties = d
        return native_push_payload_fcm_message_data

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
