from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.chronicle_link import ChronicleLink
    from ..models.opaque_chronicle_link import OpaqueChronicleLink


T = TypeVar("T", bound="ChronicleLinkFoundEvent")


@_attrs_define
class ChronicleLinkFoundEvent:
    """SSE `chronicle.link.found` on the `me` topic

    Attributes:
        chronicle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        link (ChronicleLink | OpaqueChronicleLink):
        at (datetime.datetime):
    """

    chronicle_id: str
    link: ChronicleLink | OpaqueChronicleLink
    at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.chronicle_link import ChronicleLink

        chronicle_id = self.chronicle_id

        link: dict[str, Any]
        if isinstance(self.link, ChronicleLink):
            link = self.link.to_dict()
        else:
            link = self.link.to_dict()

        at = self.at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "chronicleId": chronicle_id,
                "link": link,
                "at": at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_link import ChronicleLink
        from ..models.opaque_chronicle_link import OpaqueChronicleLink

        d = dict(src_dict)
        chronicle_id = d.pop("chronicleId")

        def _parse_link(data: object) -> ChronicleLink | OpaqueChronicleLink:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                link_type_0 = ChronicleLink.from_dict(data)

                return link_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            link_type_1 = OpaqueChronicleLink.from_dict(data)

            return link_type_1

        link = _parse_link(d.pop("link"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        chronicle_link_found_event = cls(
            chronicle_id=chronicle_id,
            link=link,
            at=at,
        )

        chronicle_link_found_event.additional_properties = d
        return chronicle_link_found_event

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
