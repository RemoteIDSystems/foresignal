from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.evidence_item_patch_source_reliability import (
    EvidenceItemPatchSourceReliability,
)
from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="EvidenceItemPatch")


@_attrs_define
class EvidenceItemPatch:
    """JSON merge patch for an evidence item (owner or editor)

    Attributes:
        title (str | Unset):
        description (None | str | Unset):
        attachment_ids (list[str] | Unset):
        url (None | str | Unset):
        source_description (None | str | Unset):
        source_reliability (EvidenceItemPatchSourceReliability | Unset): Admiralty source reliability (A completely
            reliable … F cannot be judged)
        information_credibility (int | Unset): Admiralty information credibility (1 confirmed … 6 cannot be judged)
        cited_source_id (None | str | Unset):
        cited_refs (list[str] | Unset):
        geometry (Geometry | None | Unset):
        event_time (datetime.datetime | None | Unset):
        visibility (Visibility | Unset):
        tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
    """

    title: str | Unset = UNSET
    description: None | str | Unset = UNSET
    attachment_ids: list[str] | Unset = UNSET
    url: None | str | Unset = UNSET
    source_description: None | str | Unset = UNSET
    source_reliability: EvidenceItemPatchSourceReliability | Unset = UNSET
    information_credibility: int | Unset = UNSET
    cited_source_id: None | str | Unset = UNSET
    cited_refs: list[str] | Unset = UNSET
    geometry: Geometry | None | Unset = UNSET
    event_time: datetime.datetime | None | Unset = UNSET
    visibility: Visibility | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        title = self.title

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        attachment_ids: list[str] | Unset = UNSET
        if not isinstance(self.attachment_ids, Unset):
            attachment_ids = self.attachment_ids

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        source_description: None | str | Unset
        if isinstance(self.source_description, Unset):
            source_description = UNSET
        else:
            source_description = self.source_description

        source_reliability: str | Unset = UNSET
        if not isinstance(self.source_reliability, Unset):
            source_reliability = self.source_reliability.value

        information_credibility = self.information_credibility

        cited_source_id: None | str | Unset
        if isinstance(self.cited_source_id, Unset):
            cited_source_id = UNSET
        else:
            cited_source_id = self.cited_source_id

        cited_refs: list[str] | Unset = UNSET
        if not isinstance(self.cited_refs, Unset):
            cited_refs = self.cited_refs

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        event_time: None | str | Unset
        if isinstance(self.event_time, Unset):
            event_time = UNSET
        elif isinstance(self.event_time, datetime.datetime):
            event_time = self.event_time.isoformat()
        else:
            event_time = self.event_time

        visibility: str | Unset = UNSET
        if not isinstance(self.visibility, Unset):
            visibility = self.visibility.value

        tlp: str | Unset = UNSET
        if not isinstance(self.tlp, Unset):
            tlp = self.tlp.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if title is not UNSET:
            field_dict["title"] = title
        if description is not UNSET:
            field_dict["description"] = description
        if attachment_ids is not UNSET:
            field_dict["attachmentIds"] = attachment_ids
        if url is not UNSET:
            field_dict["url"] = url
        if source_description is not UNSET:
            field_dict["sourceDescription"] = source_description
        if source_reliability is not UNSET:
            field_dict["sourceReliability"] = source_reliability
        if information_credibility is not UNSET:
            field_dict["informationCredibility"] = information_credibility
        if cited_source_id is not UNSET:
            field_dict["citedSourceId"] = cited_source_id
        if cited_refs is not UNSET:
            field_dict["citedRefs"] = cited_refs
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if event_time is not UNSET:
            field_dict["eventTime"] = event_time
        if visibility is not UNSET:
            field_dict["visibility"] = visibility
        if tlp is not UNSET:
            field_dict["tlp"] = tlp

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        title = d.pop("title", UNSET)

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        attachment_ids = cast(list[str], d.pop("attachmentIds", UNSET))

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        def _parse_source_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source_description = _parse_source_description(
            d.pop("sourceDescription", UNSET)
        )

        _source_reliability = d.pop("sourceReliability", UNSET)
        source_reliability: EvidenceItemPatchSourceReliability | Unset
        if isinstance(_source_reliability, Unset):
            source_reliability = UNSET
        else:
            source_reliability = EvidenceItemPatchSourceReliability(_source_reliability)

        information_credibility = d.pop("informationCredibility", UNSET)

        def _parse_cited_source_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        cited_source_id = _parse_cited_source_id(d.pop("citedSourceId", UNSET))

        cited_refs = cast(list[str], d.pop("citedRefs", UNSET))

        def _parse_geometry(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        def _parse_event_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                event_time_type_0 = datetime.datetime.fromisoformat(data)

                return event_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        event_time = _parse_event_time(d.pop("eventTime", UNSET))

        _visibility = d.pop("visibility", UNSET)
        visibility: Visibility | Unset
        if isinstance(_visibility, Unset):
            visibility = UNSET
        else:
            visibility = Visibility(_visibility)

        _tlp = d.pop("tlp", UNSET)
        tlp: Tlp | Unset
        if isinstance(_tlp, Unset):
            tlp = UNSET
        else:
            tlp = Tlp(_tlp)

        evidence_item_patch = cls(
            title=title,
            description=description,
            attachment_ids=attachment_ids,
            url=url,
            source_description=source_description,
            source_reliability=source_reliability,
            information_credibility=information_credibility,
            cited_source_id=cited_source_id,
            cited_refs=cited_refs,
            geometry=geometry,
            event_time=event_time,
            visibility=visibility,
            tlp=tlp,
        )

        evidence_item_patch.additional_properties = d
        return evidence_item_patch

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
