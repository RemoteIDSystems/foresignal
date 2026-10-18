from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.evidence_item_source_reliability import EvidenceItemSourceReliability
from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.geometry import Geometry
    from ..models.principal_summary import PrincipalSummary
    from ..models.provenance_chain_entry import ProvenanceChainEntry


T = TypeVar("T", bound="EvidenceItem")


@_attrs_define
class EvidenceItem:
    """User-contributed evidence graded with the Admiralty system; usable in investigations, situations, ACH (125) and
    relations (129)

        Attributes:
            id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
                `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
                `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
                situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            title (str):
            source_reliability (EvidenceItemSourceReliability): Admiralty source reliability (A completely reliable … F
                cannot be judged)
            information_credibility (int): Admiralty information credibility (1 confirmed … 6 cannot be judged)
            originator (str):  Example: operator:jsmith.
            created_by (PrincipalSummary): Public view of a principal: handle, display name, kind and avatar only
            tlp (Tlp): Traffic Light Protocol 2.0 label
            created_at (datetime.datetime):
            description (None | str | Unset): Markdown
            attachment_ids (list[str] | Unset):
            url (None | str | Unset): Link evidence; stored as metadata only and never fetched
            source_description (None | str | Unset):
            grade (str | Unset):  Example: B2.
            cited_source_id (None | str | Unset): Catalogued source this evidence cites; otherwise the originator is
                `operator:{principal}` for 116 independence
            cited_refs (list[str] | Unset): Objects this evidence cites, e.g. a report
            provenance_chain (list[ProvenanceChainEntry] | Unset):
            geometry (Geometry | None | Unset):
            event_time (datetime.datetime | None | Unset):
            tags (list[str] | Unset): Tag names visible to the caller
            owner (PrincipalSummary | Unset): Public view of a principal: handle, display name, kind and avatar only
            visibility (Visibility | Unset):
            caveats (list[Caveat] | Unset):
            updated_at (datetime.datetime | Unset):
    """

    id: str
    ref: str
    title: str
    source_reliability: EvidenceItemSourceReliability
    information_credibility: int
    originator: str
    created_by: PrincipalSummary
    tlp: Tlp
    created_at: datetime.datetime
    description: None | str | Unset = UNSET
    attachment_ids: list[str] | Unset = UNSET
    url: None | str | Unset = UNSET
    source_description: None | str | Unset = UNSET
    grade: str | Unset = UNSET
    cited_source_id: None | str | Unset = UNSET
    cited_refs: list[str] | Unset = UNSET
    provenance_chain: list[ProvenanceChainEntry] | Unset = UNSET
    geometry: Geometry | None | Unset = UNSET
    event_time: datetime.datetime | None | Unset = UNSET
    tags: list[str] | Unset = UNSET
    owner: PrincipalSummary | Unset = UNSET
    visibility: Visibility | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        id = self.id

        ref = self.ref

        title = self.title

        source_reliability = self.source_reliability.value

        information_credibility = self.information_credibility

        originator = self.originator

        created_by = self.created_by.to_dict()

        tlp = self.tlp.value

        created_at = self.created_at.isoformat()

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

        grade = self.grade

        cited_source_id: None | str | Unset
        if isinstance(self.cited_source_id, Unset):
            cited_source_id = UNSET
        else:
            cited_source_id = self.cited_source_id

        cited_refs: list[str] | Unset = UNSET
        if not isinstance(self.cited_refs, Unset):
            cited_refs = self.cited_refs

        provenance_chain: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.provenance_chain, Unset):
            provenance_chain = []
            for provenance_chain_item_data in self.provenance_chain:
                provenance_chain_item = provenance_chain_item_data.to_dict()
                provenance_chain.append(provenance_chain_item)

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

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        owner: dict[str, Any] | Unset = UNSET
        if not isinstance(self.owner, Unset):
            owner = self.owner.to_dict()

        visibility: str | Unset = UNSET
        if not isinstance(self.visibility, Unset):
            visibility = self.visibility.value

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "ref": ref,
                "title": title,
                "sourceReliability": source_reliability,
                "informationCredibility": information_credibility,
                "originator": originator,
                "createdBy": created_by,
                "tlp": tlp,
                "createdAt": created_at,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if attachment_ids is not UNSET:
            field_dict["attachmentIds"] = attachment_ids
        if url is not UNSET:
            field_dict["url"] = url
        if source_description is not UNSET:
            field_dict["sourceDescription"] = source_description
        if grade is not UNSET:
            field_dict["grade"] = grade
        if cited_source_id is not UNSET:
            field_dict["citedSourceId"] = cited_source_id
        if cited_refs is not UNSET:
            field_dict["citedRefs"] = cited_refs
        if provenance_chain is not UNSET:
            field_dict["provenanceChain"] = provenance_chain
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if event_time is not UNSET:
            field_dict["eventTime"] = event_time
        if tags is not UNSET:
            field_dict["tags"] = tags
        if owner is not UNSET:
            field_dict["owner"] = owner
        if visibility is not UNSET:
            field_dict["visibility"] = visibility
        if caveats is not UNSET:
            field_dict["caveats"] = caveats
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.geometry import Geometry
        from ..models.principal_summary import PrincipalSummary
        from ..models.provenance_chain_entry import ProvenanceChainEntry

        d = dict(src_dict)
        id = d.pop("id")

        ref = d.pop("ref")

        title = d.pop("title")

        source_reliability = EvidenceItemSourceReliability(d.pop("sourceReliability"))

        information_credibility = d.pop("informationCredibility")

        originator = d.pop("originator")

        created_by = PrincipalSummary.from_dict(d.pop("createdBy"))

        tlp = Tlp(d.pop("tlp"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

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

        grade = d.pop("grade", UNSET)

        def _parse_cited_source_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        cited_source_id = _parse_cited_source_id(d.pop("citedSourceId", UNSET))

        cited_refs = cast(list[str], d.pop("citedRefs", UNSET))

        _provenance_chain = d.pop("provenanceChain", UNSET)
        provenance_chain: list[ProvenanceChainEntry] | Unset = UNSET
        if _provenance_chain is not UNSET:
            provenance_chain = []
            for provenance_chain_item_data in _provenance_chain:
                provenance_chain_item = ProvenanceChainEntry.from_dict(
                    provenance_chain_item_data
                )

                provenance_chain.append(provenance_chain_item)

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

        tags = cast(list[str], d.pop("tags", UNSET))

        _owner = d.pop("owner", UNSET)
        owner: PrincipalSummary | Unset
        if isinstance(_owner, Unset):
            owner = UNSET
        else:
            owner = PrincipalSummary.from_dict(_owner)

        _visibility = d.pop("visibility", UNSET)
        visibility: Visibility | Unset
        if isinstance(_visibility, Unset):
            visibility = UNSET
        else:
            visibility = Visibility(_visibility)

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        evidence_item = cls(
            id=id,
            ref=ref,
            title=title,
            source_reliability=source_reliability,
            information_credibility=information_credibility,
            originator=originator,
            created_by=created_by,
            tlp=tlp,
            created_at=created_at,
            description=description,
            attachment_ids=attachment_ids,
            url=url,
            source_description=source_description,
            grade=grade,
            cited_source_id=cited_source_id,
            cited_refs=cited_refs,
            provenance_chain=provenance_chain,
            geometry=geometry,
            event_time=event_time,
            tags=tags,
            owner=owner,
            visibility=visibility,
            caveats=caveats,
            updated_at=updated_at,
        )

        evidence_item.additional_properties = d
        return evidence_item

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
