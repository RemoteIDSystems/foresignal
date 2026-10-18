from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sink_cloud_event_datacontenttype import SinkCloudEventDatacontenttype
from ..models.sink_cloud_event_specversion import SinkCloudEventSpecversion
from ..models.sink_cloud_event_type import SinkCloudEventType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.sink_cloud_event_data import SinkCloudEventData


T = TypeVar("T", bound="SinkCloudEvent")


@_attrs_define
class SinkCloudEvent:
    """CloudEvents 1.0 structured-mode event sent by the `cloudevents` sink (application/cloudevents+json; batches are
    arrays as application/cloudevents-batch+json). Signed with X-Foresignal-Signature as 018 webhooks (110)

        Attributes:
            specversion (SinkCloudEventSpecversion):
            id (str): {instanceId}:{seq}
            source (str): urn:foresignal:{instanceId}:source:{sourceId}
            type_ (SinkCloudEventType):
            subject (str): {sourceId}/{externalId}
            time (datetime.datetime): observedAt
            data (SinkCloudEventData): The version item plus attributions
            datacontenttype (SinkCloudEventDatacontenttype | Unset):
            foresignalseq (int | Unset):
            foresignalkind (str | Unset):
            foresignalreason (str | Unset): persistReason of the version, or SNAPSHOT
    """

    specversion: SinkCloudEventSpecversion
    id: str
    source: str
    type_: SinkCloudEventType
    subject: str
    time: datetime.datetime
    data: SinkCloudEventData
    datacontenttype: SinkCloudEventDatacontenttype | Unset = UNSET
    foresignalseq: int | Unset = UNSET
    foresignalkind: str | Unset = UNSET
    foresignalreason: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        specversion = self.specversion.value

        id = self.id

        source = self.source

        type_ = self.type_.value

        subject = self.subject

        time = self.time.isoformat()

        data = self.data.to_dict()

        datacontenttype: str | Unset = UNSET
        if not isinstance(self.datacontenttype, Unset):
            datacontenttype = self.datacontenttype.value

        foresignalseq = self.foresignalseq

        foresignalkind = self.foresignalkind

        foresignalreason = self.foresignalreason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "specversion": specversion,
                "id": id,
                "source": source,
                "type": type_,
                "subject": subject,
                "time": time,
                "data": data,
            }
        )
        if datacontenttype is not UNSET:
            field_dict["datacontenttype"] = datacontenttype
        if foresignalseq is not UNSET:
            field_dict["foresignalseq"] = foresignalseq
        if foresignalkind is not UNSET:
            field_dict["foresignalkind"] = foresignalkind
        if foresignalreason is not UNSET:
            field_dict["foresignalreason"] = foresignalreason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.sink_cloud_event_data import SinkCloudEventData

        d = dict(src_dict)
        specversion = SinkCloudEventSpecversion(d.pop("specversion"))

        id = d.pop("id")

        source = d.pop("source")

        type_ = SinkCloudEventType(d.pop("type"))

        subject = d.pop("subject")

        time = datetime.datetime.fromisoformat(d.pop("time"))

        data = SinkCloudEventData.from_dict(d.pop("data"))

        _datacontenttype = d.pop("datacontenttype", UNSET)
        datacontenttype: SinkCloudEventDatacontenttype | Unset
        if isinstance(_datacontenttype, Unset):
            datacontenttype = UNSET
        else:
            datacontenttype = SinkCloudEventDatacontenttype(_datacontenttype)

        foresignalseq = d.pop("foresignalseq", UNSET)

        foresignalkind = d.pop("foresignalkind", UNSET)

        foresignalreason = d.pop("foresignalreason", UNSET)

        sink_cloud_event = cls(
            specversion=specversion,
            id=id,
            source=source,
            type_=type_,
            subject=subject,
            time=time,
            data=data,
            datacontenttype=datacontenttype,
            foresignalseq=foresignalseq,
            foresignalkind=foresignalkind,
            foresignalreason=foresignalreason,
        )

        sink_cloud_event.additional_properties = d
        return sink_cloud_event

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
