from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.own_tracks_message_type import OwnTracksMessageType
from ..types import UNSET, Unset

T = TypeVar("T", bound="OwnTracksMessage")


@_attrs_define
class OwnTracksMessage:
    """OwnTracks HTTP-mode message accepted by the `owntracks` format (039). `_type=location` → kind tracker; `transition`
    → kind tracker-transition; `waypoint`, `lwt`, `card`, `status` and `encrypted` are acknowledged with `[]` and
    ignored.

        Attributes:
            field_type_ (OwnTracksMessageType):
            lat (float | Unset):
            lon (float | Unset):
            tst (int | Unset): Epoch seconds → observedAt
            acc (float | Unset): Accuracy (m) → quality.accuracyMeters
            alt (float | Unset):
            vel (float | Unset): km/h → speedMs
            cog (float | Unset):
            batt (int | Unset):
            bs (int | Unset): Battery status → charging
            conn (str | Unset):
            tid (str | Unset):
            event (str | Unset):
            desc (str | Unset):
            wtst (int | Unset):
    """

    field_type_: OwnTracksMessageType
    lat: float | Unset = UNSET
    lon: float | Unset = UNSET
    tst: int | Unset = UNSET
    acc: float | Unset = UNSET
    alt: float | Unset = UNSET
    vel: float | Unset = UNSET
    cog: float | Unset = UNSET
    batt: int | Unset = UNSET
    bs: int | Unset = UNSET
    conn: str | Unset = UNSET
    tid: str | Unset = UNSET
    event: str | Unset = UNSET
    desc: str | Unset = UNSET
    wtst: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        field_type_ = self.field_type_.value

        lat = self.lat

        lon = self.lon

        tst = self.tst

        acc = self.acc

        alt = self.alt

        vel = self.vel

        cog = self.cog

        batt = self.batt

        bs = self.bs

        conn = self.conn

        tid = self.tid

        event = self.event

        desc = self.desc

        wtst = self.wtst

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "_type": field_type_,
            }
        )
        if lat is not UNSET:
            field_dict["lat"] = lat
        if lon is not UNSET:
            field_dict["lon"] = lon
        if tst is not UNSET:
            field_dict["tst"] = tst
        if acc is not UNSET:
            field_dict["acc"] = acc
        if alt is not UNSET:
            field_dict["alt"] = alt
        if vel is not UNSET:
            field_dict["vel"] = vel
        if cog is not UNSET:
            field_dict["cog"] = cog
        if batt is not UNSET:
            field_dict["batt"] = batt
        if bs is not UNSET:
            field_dict["bs"] = bs
        if conn is not UNSET:
            field_dict["conn"] = conn
        if tid is not UNSET:
            field_dict["tid"] = tid
        if event is not UNSET:
            field_dict["event"] = event
        if desc is not UNSET:
            field_dict["desc"] = desc
        if wtst is not UNSET:
            field_dict["wtst"] = wtst

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        field_type_ = OwnTracksMessageType(d.pop("_type"))

        lat = d.pop("lat", UNSET)

        lon = d.pop("lon", UNSET)

        tst = d.pop("tst", UNSET)

        acc = d.pop("acc", UNSET)

        alt = d.pop("alt", UNSET)

        vel = d.pop("vel", UNSET)

        cog = d.pop("cog", UNSET)

        batt = d.pop("batt", UNSET)

        bs = d.pop("bs", UNSET)

        conn = d.pop("conn", UNSET)

        tid = d.pop("tid", UNSET)

        event = d.pop("event", UNSET)

        desc = d.pop("desc", UNSET)

        wtst = d.pop("wtst", UNSET)

        own_tracks_message = cls(
            field_type_=field_type_,
            lat=lat,
            lon=lon,
            tst=tst,
            acc=acc,
            alt=alt,
            vel=vel,
            cog=cog,
            batt=batt,
            bs=bs,
            conn=conn,
            tid=tid,
            event=event,
            desc=desc,
            wtst=wtst,
        )

        own_tracks_message.additional_properties = d
        return own_tracks_message

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
