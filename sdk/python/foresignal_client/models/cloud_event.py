from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.cloud_event_specversion import CloudEventSpecversion
from ..types import UNSET, Unset

T = TypeVar("T", bound="CloudEvent")


@_attrs_define
class CloudEvent:
    """CloudEvents 1.0 structured-mode event accepted by the `cloudevents` format (024)

    Attributes:
        specversion (CloudEventSpecversion):
        id (str):
        source (str):
        type_ (str):
        subject (str | Unset):
        time (datetime.datetime | Unset):
        datacontenttype (str | Unset):
        dataschema (str | Unset):
        data (Any | Unset): Payload; an object becomes the record payload (with `geometry` or `lat`/`lon`), other values
            are wrapped as `{data}`
    """

    specversion: CloudEventSpecversion
    id: str
    source: str
    type_: str
    subject: str | Unset = UNSET
    time: datetime.datetime | Unset = UNSET
    datacontenttype: str | Unset = UNSET
    dataschema: str | Unset = UNSET
    data: Any | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        specversion = self.specversion.value

        id = self.id

        source = self.source

        type_ = self.type_

        subject = self.subject

        time: str | Unset = UNSET
        if not isinstance(self.time, Unset):
            time = self.time.isoformat()

        datacontenttype = self.datacontenttype

        dataschema = self.dataschema

        data = self.data

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "specversion": specversion,
                "id": id,
                "source": source,
                "type": type_,
            }
        )
        if subject is not UNSET:
            field_dict["subject"] = subject
        if time is not UNSET:
            field_dict["time"] = time
        if datacontenttype is not UNSET:
            field_dict["datacontenttype"] = datacontenttype
        if dataschema is not UNSET:
            field_dict["dataschema"] = dataschema
        if data is not UNSET:
            field_dict["data"] = data

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        specversion = CloudEventSpecversion(d.pop("specversion"))

        id = d.pop("id")

        source = d.pop("source")

        type_ = d.pop("type")

        subject = d.pop("subject", UNSET)

        _time = d.pop("time", UNSET)
        time: datetime.datetime | Unset
        if isinstance(_time, Unset):
            time = UNSET
        else:
            time = datetime.datetime.fromisoformat(_time)

        datacontenttype = d.pop("datacontenttype", UNSET)

        dataschema = d.pop("dataschema", UNSET)

        data = d.pop("data", UNSET)

        cloud_event = cls(
            specversion=specversion,
            id=id,
            source=source,
            type_=type_,
            subject=subject,
            time=time,
            datacontenttype=datacontenttype,
            dataschema=dataschema,
            data=data,
        )

        cloud_event.additional_properties = d
        return cloud_event

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
