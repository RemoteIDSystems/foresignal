from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.nws_alert_payload_geometry_source import NwsAlertPayloadGeometrySource
from ..models.nws_alert_payload_message_type import NwsAlertPayloadMessageType
from ..models.nws_alert_payload_severity import NwsAlertPayloadSeverity
from ..types import UNSET, Unset

T = TypeVar("T", bound="NwsAlertPayload")


@_attrs_define
class NwsAlertPayload:
    """Payload of `nws-alert` records (nws-alerts, externalId = properties.id); null fields omitted

    Attributes:
        event (str):
        severity (NwsAlertPayloadSeverity):
        event_code (None | str | Unset):
        headline (str | Unset):
        certainty (str | Unset):
        urgency (str | Unset):
        message_type (NwsAlertPayloadMessageType | Unset):
        category (str | Unset):
        response (str | Unset):
        sender_name (str | Unset):
        area_desc (str | Unset):
        sent (datetime.datetime | Unset):
        effective (datetime.datetime | Unset):
        onset (datetime.datetime | Unset):
        expires (datetime.datetime | Unset):
        ends (datetime.datetime | Unset):
        ugc (list[str] | Unset):
        same (list[str] | Unset):
        vtec (list[str] | Unset):
        motion (str | Unset): eventMotionDescription
        max_hail_size_in (float | Unset):
        max_wind_gust_mph (float | Unset):
        tornado_detection (str | Unset):
        thunderstorm_damage_threat (str | Unset):
        description (str | Unset): Untrusted upstream text, ≤ 8 KB
        instruction (str | Unset): Untrusted upstream text, ≤ 8 KB
        web (str | Unset):
        geometry_source (NwsAlertPayloadGeometrySource | Unset):
    """

    event: str
    severity: NwsAlertPayloadSeverity
    event_code: None | str | Unset = UNSET
    headline: str | Unset = UNSET
    certainty: str | Unset = UNSET
    urgency: str | Unset = UNSET
    message_type: NwsAlertPayloadMessageType | Unset = UNSET
    category: str | Unset = UNSET
    response: str | Unset = UNSET
    sender_name: str | Unset = UNSET
    area_desc: str | Unset = UNSET
    sent: datetime.datetime | Unset = UNSET
    effective: datetime.datetime | Unset = UNSET
    onset: datetime.datetime | Unset = UNSET
    expires: datetime.datetime | Unset = UNSET
    ends: datetime.datetime | Unset = UNSET
    ugc: list[str] | Unset = UNSET
    same: list[str] | Unset = UNSET
    vtec: list[str] | Unset = UNSET
    motion: str | Unset = UNSET
    max_hail_size_in: float | Unset = UNSET
    max_wind_gust_mph: float | Unset = UNSET
    tornado_detection: str | Unset = UNSET
    thunderstorm_damage_threat: str | Unset = UNSET
    description: str | Unset = UNSET
    instruction: str | Unset = UNSET
    web: str | Unset = UNSET
    geometry_source: NwsAlertPayloadGeometrySource | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event

        severity = self.severity.value

        event_code: None | str | Unset
        if isinstance(self.event_code, Unset):
            event_code = UNSET
        else:
            event_code = self.event_code

        headline = self.headline

        certainty = self.certainty

        urgency = self.urgency

        message_type: str | Unset = UNSET
        if not isinstance(self.message_type, Unset):
            message_type = self.message_type.value

        category = self.category

        response = self.response

        sender_name = self.sender_name

        area_desc = self.area_desc

        sent: str | Unset = UNSET
        if not isinstance(self.sent, Unset):
            sent = self.sent.isoformat()

        effective: str | Unset = UNSET
        if not isinstance(self.effective, Unset):
            effective = self.effective.isoformat()

        onset: str | Unset = UNSET
        if not isinstance(self.onset, Unset):
            onset = self.onset.isoformat()

        expires: str | Unset = UNSET
        if not isinstance(self.expires, Unset):
            expires = self.expires.isoformat()

        ends: str | Unset = UNSET
        if not isinstance(self.ends, Unset):
            ends = self.ends.isoformat()

        ugc: list[str] | Unset = UNSET
        if not isinstance(self.ugc, Unset):
            ugc = self.ugc

        same: list[str] | Unset = UNSET
        if not isinstance(self.same, Unset):
            same = self.same

        vtec: list[str] | Unset = UNSET
        if not isinstance(self.vtec, Unset):
            vtec = self.vtec

        motion = self.motion

        max_hail_size_in = self.max_hail_size_in

        max_wind_gust_mph = self.max_wind_gust_mph

        tornado_detection = self.tornado_detection

        thunderstorm_damage_threat = self.thunderstorm_damage_threat

        description = self.description

        instruction = self.instruction

        web = self.web

        geometry_source: str | Unset = UNSET
        if not isinstance(self.geometry_source, Unset):
            geometry_source = self.geometry_source.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "severity": severity,
            }
        )
        if event_code is not UNSET:
            field_dict["eventCode"] = event_code
        if headline is not UNSET:
            field_dict["headline"] = headline
        if certainty is not UNSET:
            field_dict["certainty"] = certainty
        if urgency is not UNSET:
            field_dict["urgency"] = urgency
        if message_type is not UNSET:
            field_dict["messageType"] = message_type
        if category is not UNSET:
            field_dict["category"] = category
        if response is not UNSET:
            field_dict["response"] = response
        if sender_name is not UNSET:
            field_dict["senderName"] = sender_name
        if area_desc is not UNSET:
            field_dict["areaDesc"] = area_desc
        if sent is not UNSET:
            field_dict["sent"] = sent
        if effective is not UNSET:
            field_dict["effective"] = effective
        if onset is not UNSET:
            field_dict["onset"] = onset
        if expires is not UNSET:
            field_dict["expires"] = expires
        if ends is not UNSET:
            field_dict["ends"] = ends
        if ugc is not UNSET:
            field_dict["ugc"] = ugc
        if same is not UNSET:
            field_dict["same"] = same
        if vtec is not UNSET:
            field_dict["vtec"] = vtec
        if motion is not UNSET:
            field_dict["motion"] = motion
        if max_hail_size_in is not UNSET:
            field_dict["maxHailSizeIn"] = max_hail_size_in
        if max_wind_gust_mph is not UNSET:
            field_dict["maxWindGustMph"] = max_wind_gust_mph
        if tornado_detection is not UNSET:
            field_dict["tornadoDetection"] = tornado_detection
        if thunderstorm_damage_threat is not UNSET:
            field_dict["thunderstormDamageThreat"] = thunderstorm_damage_threat
        if description is not UNSET:
            field_dict["description"] = description
        if instruction is not UNSET:
            field_dict["instruction"] = instruction
        if web is not UNSET:
            field_dict["web"] = web
        if geometry_source is not UNSET:
            field_dict["geometrySource"] = geometry_source

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        event = d.pop("event")

        severity = NwsAlertPayloadSeverity(d.pop("severity"))

        def _parse_event_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        event_code = _parse_event_code(d.pop("eventCode", UNSET))

        headline = d.pop("headline", UNSET)

        certainty = d.pop("certainty", UNSET)

        urgency = d.pop("urgency", UNSET)

        _message_type = d.pop("messageType", UNSET)
        message_type: NwsAlertPayloadMessageType | Unset
        if isinstance(_message_type, Unset):
            message_type = UNSET
        else:
            message_type = NwsAlertPayloadMessageType(_message_type)

        category = d.pop("category", UNSET)

        response = d.pop("response", UNSET)

        sender_name = d.pop("senderName", UNSET)

        area_desc = d.pop("areaDesc", UNSET)

        _sent = d.pop("sent", UNSET)
        sent: datetime.datetime | Unset
        if isinstance(_sent, Unset):
            sent = UNSET
        else:
            sent = datetime.datetime.fromisoformat(_sent)

        _effective = d.pop("effective", UNSET)
        effective: datetime.datetime | Unset
        if isinstance(_effective, Unset):
            effective = UNSET
        else:
            effective = datetime.datetime.fromisoformat(_effective)

        _onset = d.pop("onset", UNSET)
        onset: datetime.datetime | Unset
        if isinstance(_onset, Unset):
            onset = UNSET
        else:
            onset = datetime.datetime.fromisoformat(_onset)

        _expires = d.pop("expires", UNSET)
        expires: datetime.datetime | Unset
        if isinstance(_expires, Unset):
            expires = UNSET
        else:
            expires = datetime.datetime.fromisoformat(_expires)

        _ends = d.pop("ends", UNSET)
        ends: datetime.datetime | Unset
        if isinstance(_ends, Unset):
            ends = UNSET
        else:
            ends = datetime.datetime.fromisoformat(_ends)

        ugc = cast(list[str], d.pop("ugc", UNSET))

        same = cast(list[str], d.pop("same", UNSET))

        vtec = cast(list[str], d.pop("vtec", UNSET))

        motion = d.pop("motion", UNSET)

        max_hail_size_in = d.pop("maxHailSizeIn", UNSET)

        max_wind_gust_mph = d.pop("maxWindGustMph", UNSET)

        tornado_detection = d.pop("tornadoDetection", UNSET)

        thunderstorm_damage_threat = d.pop("thunderstormDamageThreat", UNSET)

        description = d.pop("description", UNSET)

        instruction = d.pop("instruction", UNSET)

        web = d.pop("web", UNSET)

        _geometry_source = d.pop("geometrySource", UNSET)
        geometry_source: NwsAlertPayloadGeometrySource | Unset
        if isinstance(_geometry_source, Unset):
            geometry_source = UNSET
        else:
            geometry_source = NwsAlertPayloadGeometrySource(_geometry_source)

        nws_alert_payload = cls(
            event=event,
            severity=severity,
            event_code=event_code,
            headline=headline,
            certainty=certainty,
            urgency=urgency,
            message_type=message_type,
            category=category,
            response=response,
            sender_name=sender_name,
            area_desc=area_desc,
            sent=sent,
            effective=effective,
            onset=onset,
            expires=expires,
            ends=ends,
            ugc=ugc,
            same=same,
            vtec=vtec,
            motion=motion,
            max_hail_size_in=max_hail_size_in,
            max_wind_gust_mph=max_wind_gust_mph,
            tornado_detection=tornado_detection,
            thunderstorm_damage_threat=thunderstorm_damage_threat,
            description=description,
            instruction=instruction,
            web=web,
            geometry_source=geometry_source,
        )

        nws_alert_payload.additional_properties = d
        return nws_alert_payload

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
