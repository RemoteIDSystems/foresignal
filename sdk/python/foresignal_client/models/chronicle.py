from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_cap_state import ChronicleCapState
from ..models.chronicle_discoverability import ChronicleDiscoverability
from ..models.chronicle_origin import ChronicleOrigin
from ..models.chronicle_status import ChronicleStatus
from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_archive_type_0 import ChronicleArchiveType0
    from ..models.chronicle_area import ChronicleArea
    from ..models.chronicle_layer import ChronicleLayer
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="Chronicle")


@_attrs_define
class Chronicle:
    """A named, shareable monitoring thread that records its own history while active

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        display_id (str):  Example: CHR-2026-0007.
        name (str):
        owner (PrincipalSummary): Public view of a principal: handle, display name, kind and avatar only
        visibility (Visibility):
        tlp (Tlp): Traffic Light Protocol 2.0 label
        status (ChronicleStatus):
        retention (str): `forever` (default) or an ISO-8601 duration kept after stop Example: forever.
        size_cap_bytes (int):
        bytes_used (int):
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        origin (ChronicleOrigin):
        layers (list[ChronicleLayer]):
        version (int): Optimistic-lock version; sent as ETag
        created_at (datetime.datetime):
        description (None | str | Unset):
        area (ChronicleArea | None | Unset):
        started_at (datetime.datetime | None | Unset):
        stopped_at (datetime.datetime | None | Unset):
        capture_from_seq (int | None | Unset):
        capture_to_seq (int | None | Unset):
        cap_state (ChronicleCapState | Unset): `warning` at 90% of the cap; `exceeded` switches full and downsampled
            layers to events-only
        situation_ids (list[str] | Unset):
        hypothesis_set_ids (list[str] | Unset):
        investigation_ids (list[str] | Unset):
        topic_ids (list[str] | Unset): 117 attention topic ids
        proposal_id (None | str | Unset):
        archive (ChronicleArchiveType0 | None | Unset):
        discoverability (ChronicleDiscoverability | Unset): Owner's discovery setting, capped by TLP (134): the
            effective value
        reveal_title (bool | Unset): Show the title in opaque links (134)
        reveal_owner (bool | Unset): Show the owner handle in opaque links (134)
        unread_count (int | Unset): Unread thread items for the caller
        tags (list[str] | Unset): Caller's visible tags
        updated_at (datetime.datetime | Unset):
    """

    id: str
    ref: str
    display_id: str
    name: str
    owner: PrincipalSummary
    visibility: Visibility
    tlp: Tlp
    status: ChronicleStatus
    retention: str
    size_cap_bytes: int
    bytes_used: int
    workspace_id: str
    origin: ChronicleOrigin
    layers: list[ChronicleLayer]
    version: int
    created_at: datetime.datetime
    description: None | str | Unset = UNSET
    area: ChronicleArea | None | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    stopped_at: datetime.datetime | None | Unset = UNSET
    capture_from_seq: int | None | Unset = UNSET
    capture_to_seq: int | None | Unset = UNSET
    cap_state: ChronicleCapState | Unset = UNSET
    situation_ids: list[str] | Unset = UNSET
    hypothesis_set_ids: list[str] | Unset = UNSET
    investigation_ids: list[str] | Unset = UNSET
    topic_ids: list[str] | Unset = UNSET
    proposal_id: None | str | Unset = UNSET
    archive: ChronicleArchiveType0 | None | Unset = UNSET
    discoverability: ChronicleDiscoverability | Unset = UNSET
    reveal_title: bool | Unset = UNSET
    reveal_owner: bool | Unset = UNSET
    unread_count: int | Unset = UNSET
    tags: list[str] | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.chronicle_archive_type_0 import ChronicleArchiveType0
        from ..models.chronicle_area import ChronicleArea

        id = self.id

        ref = self.ref

        display_id = self.display_id

        name = self.name

        owner = self.owner.to_dict()

        visibility = self.visibility.value

        tlp = self.tlp.value

        status = self.status.value

        retention = self.retention

        size_cap_bytes = self.size_cap_bytes

        bytes_used = self.bytes_used

        workspace_id = self.workspace_id

        origin = self.origin.value

        layers = []
        for layers_item_data in self.layers:
            layers_item = layers_item_data.to_dict()
            layers.append(layers_item)

        version = self.version

        created_at = self.created_at.isoformat()

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        area: dict[str, Any] | None | Unset
        if isinstance(self.area, Unset):
            area = UNSET
        elif isinstance(self.area, ChronicleArea):
            area = self.area.to_dict()
        else:
            area = self.area

        started_at: None | str | Unset
        if isinstance(self.started_at, Unset):
            started_at = UNSET
        elif isinstance(self.started_at, datetime.datetime):
            started_at = self.started_at.isoformat()
        else:
            started_at = self.started_at

        stopped_at: None | str | Unset
        if isinstance(self.stopped_at, Unset):
            stopped_at = UNSET
        elif isinstance(self.stopped_at, datetime.datetime):
            stopped_at = self.stopped_at.isoformat()
        else:
            stopped_at = self.stopped_at

        capture_from_seq: int | None | Unset
        if isinstance(self.capture_from_seq, Unset):
            capture_from_seq = UNSET
        else:
            capture_from_seq = self.capture_from_seq

        capture_to_seq: int | None | Unset
        if isinstance(self.capture_to_seq, Unset):
            capture_to_seq = UNSET
        else:
            capture_to_seq = self.capture_to_seq

        cap_state: str | Unset = UNSET
        if not isinstance(self.cap_state, Unset):
            cap_state = self.cap_state.value

        situation_ids: list[str] | Unset = UNSET
        if not isinstance(self.situation_ids, Unset):
            situation_ids = self.situation_ids

        hypothesis_set_ids: list[str] | Unset = UNSET
        if not isinstance(self.hypothesis_set_ids, Unset):
            hypothesis_set_ids = self.hypothesis_set_ids

        investigation_ids: list[str] | Unset = UNSET
        if not isinstance(self.investigation_ids, Unset):
            investigation_ids = self.investigation_ids

        topic_ids: list[str] | Unset = UNSET
        if not isinstance(self.topic_ids, Unset):
            topic_ids = self.topic_ids

        proposal_id: None | str | Unset
        if isinstance(self.proposal_id, Unset):
            proposal_id = UNSET
        else:
            proposal_id = self.proposal_id

        archive: dict[str, Any] | None | Unset
        if isinstance(self.archive, Unset):
            archive = UNSET
        elif isinstance(self.archive, ChronicleArchiveType0):
            archive = self.archive.to_dict()
        else:
            archive = self.archive

        discoverability: str | Unset = UNSET
        if not isinstance(self.discoverability, Unset):
            discoverability = self.discoverability.value

        reveal_title = self.reveal_title

        reveal_owner = self.reveal_owner

        unread_count = self.unread_count

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "ref": ref,
                "displayId": display_id,
                "name": name,
                "owner": owner,
                "visibility": visibility,
                "tlp": tlp,
                "status": status,
                "retention": retention,
                "sizeCapBytes": size_cap_bytes,
                "bytesUsed": bytes_used,
                "workspaceId": workspace_id,
                "origin": origin,
                "layers": layers,
                "version": version,
                "createdAt": created_at,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if area is not UNSET:
            field_dict["area"] = area
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if stopped_at is not UNSET:
            field_dict["stoppedAt"] = stopped_at
        if capture_from_seq is not UNSET:
            field_dict["captureFromSeq"] = capture_from_seq
        if capture_to_seq is not UNSET:
            field_dict["captureToSeq"] = capture_to_seq
        if cap_state is not UNSET:
            field_dict["capState"] = cap_state
        if situation_ids is not UNSET:
            field_dict["situationIds"] = situation_ids
        if hypothesis_set_ids is not UNSET:
            field_dict["hypothesisSetIds"] = hypothesis_set_ids
        if investigation_ids is not UNSET:
            field_dict["investigationIds"] = investigation_ids
        if topic_ids is not UNSET:
            field_dict["topicIds"] = topic_ids
        if proposal_id is not UNSET:
            field_dict["proposalId"] = proposal_id
        if archive is not UNSET:
            field_dict["archive"] = archive
        if discoverability is not UNSET:
            field_dict["discoverability"] = discoverability
        if reveal_title is not UNSET:
            field_dict["revealTitle"] = reveal_title
        if reveal_owner is not UNSET:
            field_dict["revealOwner"] = reveal_owner
        if unread_count is not UNSET:
            field_dict["unreadCount"] = unread_count
        if tags is not UNSET:
            field_dict["tags"] = tags
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_archive_type_0 import ChronicleArchiveType0
        from ..models.chronicle_area import ChronicleArea
        from ..models.chronicle_layer import ChronicleLayer
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        id = d.pop("id")

        ref = d.pop("ref")

        display_id = d.pop("displayId")

        name = d.pop("name")

        owner = PrincipalSummary.from_dict(d.pop("owner"))

        visibility = Visibility(d.pop("visibility"))

        tlp = Tlp(d.pop("tlp"))

        status = ChronicleStatus(d.pop("status"))

        retention = d.pop("retention")

        size_cap_bytes = d.pop("sizeCapBytes")

        bytes_used = d.pop("bytesUsed")

        workspace_id = d.pop("workspaceId")

        origin = ChronicleOrigin(d.pop("origin"))

        layers = []
        _layers = d.pop("layers")
        for layers_item_data in _layers:
            layers_item = ChronicleLayer.from_dict(layers_item_data)

            layers.append(layers_item)

        version = d.pop("version")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        def _parse_area(data: object) -> ChronicleArea | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                area_type_0 = ChronicleArea.from_dict(data)

                return area_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ChronicleArea | None | Unset, data)

        area = _parse_area(d.pop("area", UNSET))

        def _parse_started_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                started_at_type_0 = datetime.datetime.fromisoformat(data)

                return started_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        started_at = _parse_started_at(d.pop("startedAt", UNSET))

        def _parse_stopped_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                stopped_at_type_0 = datetime.datetime.fromisoformat(data)

                return stopped_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        stopped_at = _parse_stopped_at(d.pop("stoppedAt", UNSET))

        def _parse_capture_from_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        capture_from_seq = _parse_capture_from_seq(d.pop("captureFromSeq", UNSET))

        def _parse_capture_to_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        capture_to_seq = _parse_capture_to_seq(d.pop("captureToSeq", UNSET))

        _cap_state = d.pop("capState", UNSET)
        cap_state: ChronicleCapState | Unset
        if isinstance(_cap_state, Unset):
            cap_state = UNSET
        else:
            cap_state = ChronicleCapState(_cap_state)

        situation_ids = cast(list[str], d.pop("situationIds", UNSET))

        hypothesis_set_ids = cast(list[str], d.pop("hypothesisSetIds", UNSET))

        investigation_ids = cast(list[str], d.pop("investigationIds", UNSET))

        topic_ids = cast(list[str], d.pop("topicIds", UNSET))

        def _parse_proposal_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        proposal_id = _parse_proposal_id(d.pop("proposalId", UNSET))

        def _parse_archive(data: object) -> ChronicleArchiveType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                archive_type_0 = ChronicleArchiveType0.from_dict(data)

                return archive_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ChronicleArchiveType0 | None | Unset, data)

        archive = _parse_archive(d.pop("archive", UNSET))

        _discoverability = d.pop("discoverability", UNSET)
        discoverability: ChronicleDiscoverability | Unset
        if isinstance(_discoverability, Unset):
            discoverability = UNSET
        else:
            discoverability = ChronicleDiscoverability(_discoverability)

        reveal_title = d.pop("revealTitle", UNSET)

        reveal_owner = d.pop("revealOwner", UNSET)

        unread_count = d.pop("unreadCount", UNSET)

        tags = cast(list[str], d.pop("tags", UNSET))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        chronicle = cls(
            id=id,
            ref=ref,
            display_id=display_id,
            name=name,
            owner=owner,
            visibility=visibility,
            tlp=tlp,
            status=status,
            retention=retention,
            size_cap_bytes=size_cap_bytes,
            bytes_used=bytes_used,
            workspace_id=workspace_id,
            origin=origin,
            layers=layers,
            version=version,
            created_at=created_at,
            description=description,
            area=area,
            started_at=started_at,
            stopped_at=stopped_at,
            capture_from_seq=capture_from_seq,
            capture_to_seq=capture_to_seq,
            cap_state=cap_state,
            situation_ids=situation_ids,
            hypothesis_set_ids=hypothesis_set_ids,
            investigation_ids=investigation_ids,
            topic_ids=topic_ids,
            proposal_id=proposal_id,
            archive=archive,
            discoverability=discoverability,
            reveal_title=reveal_title,
            reveal_owner=reveal_owner,
            unread_count=unread_count,
            tags=tags,
            updated_at=updated_at,
        )

        chronicle.additional_properties = d
        return chronicle

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
