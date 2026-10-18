from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.canonical_event import CanonicalEvent
    from ..models.observation_link import ObservationLink
    from ..models.originator import Originator


T = TypeVar("T", bound="RecordEvidence")


@_attrs_define
class RecordEvidence:
    """Evidence identity of one record: its originator, links and canonical events (116)

    Attributes:
        source_id (str):
        external_id (str):
        originator (Originator): Who originated an observation (116)
        links (list[ObservationLink]):
        canonical_events (list[CanonicalEvent]): Summaries
    """

    source_id: str
    external_id: str
    originator: Originator
    links: list[ObservationLink]
    canonical_events: list[CanonicalEvent]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        external_id = self.external_id

        originator = self.originator.to_dict()

        links = []
        for links_item_data in self.links:
            links_item = links_item_data.to_dict()
            links.append(links_item)

        canonical_events = []
        for canonical_events_item_data in self.canonical_events:
            canonical_events_item = canonical_events_item_data.to_dict()
            canonical_events.append(canonical_events_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "externalId": external_id,
                "originator": originator,
                "links": links,
                "canonicalEvents": canonical_events,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.canonical_event import CanonicalEvent
        from ..models.observation_link import ObservationLink
        from ..models.originator import Originator

        d = dict(src_dict)
        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        originator = Originator.from_dict(d.pop("originator"))

        links = []
        _links = d.pop("links")
        for links_item_data in _links:
            links_item = ObservationLink.from_dict(links_item_data)

            links.append(links_item)

        canonical_events = []
        _canonical_events = d.pop("canonicalEvents")
        for canonical_events_item_data in _canonical_events:
            canonical_events_item = CanonicalEvent.from_dict(canonical_events_item_data)

            canonical_events.append(canonical_events_item)

        record_evidence = cls(
            source_id=source_id,
            external_id=external_id,
            originator=originator,
            links=links,
            canonical_events=canonical_events,
        )

        record_evidence.additional_properties = d
        return record_evidence

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
