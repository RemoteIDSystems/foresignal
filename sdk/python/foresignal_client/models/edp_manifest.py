from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.edp_source_info import EdpSourceInfo
    from ..models.edp_stream import EdpStream
    from ..models.edp_transport_type_0 import EdpTransportType0
    from ..models.edp_transport_type_1 import EdpTransportType1


T = TypeVar("T", bound="EdpManifest")


@_attrs_define
class EdpManifest:
    """Self-description of an EDP provider (docs/External-Data-Protocol.md §2). Stream ids must be unique and each layer
    stream must carry feature or event items (checked at registration). Unknown fields are ignored at every level
    (forward compatibility within 1.x).

        Attributes:
            protocol (str): foresignal-edp/1.N; N must not exceed the highest minor the instance supports (GET
                /v1/external/protocol), else PROTOCOL_UNSUPPORTED
            source (EdpSourceInfo): Identity, publisher, licence, access and originator of a provider
            transport (EdpTransportType0 | EdpTransportType1): How items reach Foresignal: at least one of push: true or a
                pull endpoint
            streams (list[EdpStream]):
    """

    protocol: str
    source: EdpSourceInfo
    transport: EdpTransportType0 | EdpTransportType1
    streams: list[EdpStream]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.edp_transport_type_0 import EdpTransportType0

        protocol = self.protocol

        source = self.source.to_dict()

        transport: dict[str, Any]
        if isinstance(self.transport, EdpTransportType0):
            transport = self.transport.to_dict()
        else:
            transport = self.transport.to_dict()

        streams = []
        for streams_item_data in self.streams:
            streams_item = streams_item_data.to_dict()
            streams.append(streams_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "protocol": protocol,
                "source": source,
                "transport": transport,
                "streams": streams,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_source_info import EdpSourceInfo
        from ..models.edp_stream import EdpStream
        from ..models.edp_transport_type_0 import EdpTransportType0
        from ..models.edp_transport_type_1 import EdpTransportType1

        d = dict(src_dict)
        protocol = d.pop("protocol")

        source = EdpSourceInfo.from_dict(d.pop("source"))

        def _parse_transport(data: object) -> EdpTransportType0 | EdpTransportType1:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_edp_transport_type_0 = EdpTransportType0.from_dict(
                    data
                )

                return componentsschemas_edp_transport_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            componentsschemas_edp_transport_type_1 = EdpTransportType1.from_dict(data)

            return componentsschemas_edp_transport_type_1

        transport = _parse_transport(d.pop("transport"))

        streams = []
        _streams = d.pop("streams")
        for streams_item_data in _streams:
            streams_item = EdpStream.from_dict(streams_item_data)

            streams.append(streams_item)

        edp_manifest = cls(
            protocol=protocol,
            source=source,
            transport=transport,
            streams=streams,
        )

        edp_manifest.additional_properties = d
        return edp_manifest

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
