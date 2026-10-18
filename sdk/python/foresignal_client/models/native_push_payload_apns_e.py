from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.native_push_payload_apns_e_ev import NativePushPayloadApnsEEv
from ..models.native_push_payload_apns_es import NativePushPayloadApnsES
from ..models.native_push_payload_apns_ev import NativePushPayloadApnsEV
from ..types import UNSET, Unset

T = TypeVar("T", bound="NativePushPayloadApnsE")


@_attrs_define
class NativePushPayloadApnsE:
    """Foresignal ids

    Attributes:
        v (NativePushPayloadApnsEV):
        id (int): The notification id (0 for a test push)
        s (NativePushPayloadApnsES):
        ev (NativePushPayloadApnsEEv):
        a (int | None | Unset):
        l (str | Unset):
    """

    v: NativePushPayloadApnsEV
    id: int
    s: NativePushPayloadApnsES
    ev: NativePushPayloadApnsEEv
    a: int | None | Unset = UNSET
    l: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        v = self.v.value

        id = self.id

        s = self.s.value

        ev = self.ev.value

        a: int | None | Unset
        if isinstance(self.a, Unset):
            a = UNSET
        else:
            a = self.a

        l = self.l

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "v": v,
                "id": id,
                "s": s,
                "ev": ev,
            }
        )
        if a is not UNSET:
            field_dict["a"] = a
        if l is not UNSET:
            field_dict["l"] = l

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        v = NativePushPayloadApnsEV(d.pop("v"))

        id = d.pop("id")

        s = NativePushPayloadApnsES(d.pop("s"))

        ev = NativePushPayloadApnsEEv(d.pop("ev"))

        def _parse_a(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        a = _parse_a(d.pop("a", UNSET))

        l = d.pop("l", UNSET)

        native_push_payload_apns_e = cls(
            v=v,
            id=id,
            s=s,
            ev=ev,
            a=a,
            l=l,
        )

        native_push_payload_apns_e.additional_properties = d
        return native_push_payload_apns_e

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
