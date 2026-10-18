from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.investigation_status import InvestigationStatus
from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.investigation_item import InvestigationItem
    from ..models.object_acl_entry import ObjectAclEntry
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="Investigation")


@_attrs_define
class Investigation:
    """An operator case file (renamed from 'case'); 130 adds owners, access lists, TLP and links

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        display_id (str):  Example: INV-2026-0042.
        title (str):
        status (InvestigationStatus):
        created_by (str):
        created_at (datetime.datetime):
        updated_at (datetime.datetime):
        summary (None | str | Unset): Operator-authored markdown
        tags (list[str] | Unset):
        item_count (int | Unset):
        items (list[InvestigationItem] | Unset): Present on getInvestigation
        owner (PrincipalSummary | Unset): Public view of a principal: handle, display name, kind and avatar only
        visibility (Visibility | Unset):
        tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
        members (list[ObjectAclEntry] | Unset): Access-list grants (viewer, commenter, editor, owner)
        workspace_id (None | str | Unset):
        hypothesis_set_ids (list[str] | Unset):
        question_ids (list[str] | Unset):
        chronicle_ids (list[str] | Unset):
        report_ids (list[str] | Unset):
    """

    id: str
    display_id: str
    title: str
    status: InvestigationStatus
    created_by: str
    created_at: datetime.datetime
    updated_at: datetime.datetime
    summary: None | str | Unset = UNSET
    tags: list[str] | Unset = UNSET
    item_count: int | Unset = UNSET
    items: list[InvestigationItem] | Unset = UNSET
    owner: PrincipalSummary | Unset = UNSET
    visibility: Visibility | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    members: list[ObjectAclEntry] | Unset = UNSET
    workspace_id: None | str | Unset = UNSET
    hypothesis_set_ids: list[str] | Unset = UNSET
    question_ids: list[str] | Unset = UNSET
    chronicle_ids: list[str] | Unset = UNSET
    report_ids: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        display_id = self.display_id

        title = self.title

        status = self.status.value

        created_by = self.created_by

        created_at = self.created_at.isoformat()

        updated_at = self.updated_at.isoformat()

        summary: None | str | Unset
        if isinstance(self.summary, Unset):
            summary = UNSET
        else:
            summary = self.summary

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        item_count = self.item_count

        items: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.items, Unset):
            items = []
            for items_item_data in self.items:
                items_item = items_item_data.to_dict()
                items.append(items_item)

        owner: dict[str, Any] | Unset = UNSET
        if not isinstance(self.owner, Unset):
            owner = self.owner.to_dict()

        visibility: str | Unset = UNSET
        if not isinstance(self.visibility, Unset):
            visibility = self.visibility.value

        tlp: str | Unset = UNSET
        if not isinstance(self.tlp, Unset):
            tlp = self.tlp.value

        members: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.members, Unset):
            members = []
            for members_item_data in self.members:
                members_item = members_item_data.to_dict()
                members.append(members_item)

        workspace_id: None | str | Unset
        if isinstance(self.workspace_id, Unset):
            workspace_id = UNSET
        else:
            workspace_id = self.workspace_id

        hypothesis_set_ids: list[str] | Unset = UNSET
        if not isinstance(self.hypothesis_set_ids, Unset):
            hypothesis_set_ids = self.hypothesis_set_ids

        question_ids: list[str] | Unset = UNSET
        if not isinstance(self.question_ids, Unset):
            question_ids = self.question_ids

        chronicle_ids: list[str] | Unset = UNSET
        if not isinstance(self.chronicle_ids, Unset):
            chronicle_ids = self.chronicle_ids

        report_ids: list[str] | Unset = UNSET
        if not isinstance(self.report_ids, Unset):
            report_ids = self.report_ids

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "displayId": display_id,
                "title": title,
                "status": status,
                "createdBy": created_by,
                "createdAt": created_at,
                "updatedAt": updated_at,
            }
        )
        if summary is not UNSET:
            field_dict["summary"] = summary
        if tags is not UNSET:
            field_dict["tags"] = tags
        if item_count is not UNSET:
            field_dict["itemCount"] = item_count
        if items is not UNSET:
            field_dict["items"] = items
        if owner is not UNSET:
            field_dict["owner"] = owner
        if visibility is not UNSET:
            field_dict["visibility"] = visibility
        if tlp is not UNSET:
            field_dict["tlp"] = tlp
        if members is not UNSET:
            field_dict["members"] = members
        if workspace_id is not UNSET:
            field_dict["workspaceId"] = workspace_id
        if hypothesis_set_ids is not UNSET:
            field_dict["hypothesisSetIds"] = hypothesis_set_ids
        if question_ids is not UNSET:
            field_dict["questionIds"] = question_ids
        if chronicle_ids is not UNSET:
            field_dict["chronicleIds"] = chronicle_ids
        if report_ids is not UNSET:
            field_dict["reportIds"] = report_ids

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.investigation_item import InvestigationItem
        from ..models.object_acl_entry import ObjectAclEntry
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        id = d.pop("id")

        display_id = d.pop("displayId")

        title = d.pop("title")

        status = InvestigationStatus(d.pop("status"))

        created_by = d.pop("createdBy")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        def _parse_summary(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        summary = _parse_summary(d.pop("summary", UNSET))

        tags = cast(list[str], d.pop("tags", UNSET))

        item_count = d.pop("itemCount", UNSET)

        _items = d.pop("items", UNSET)
        items: list[InvestigationItem] | Unset = UNSET
        if _items is not UNSET:
            items = []
            for items_item_data in _items:
                items_item = InvestigationItem.from_dict(items_item_data)

                items.append(items_item)

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

        _tlp = d.pop("tlp", UNSET)
        tlp: Tlp | Unset
        if isinstance(_tlp, Unset):
            tlp = UNSET
        else:
            tlp = Tlp(_tlp)

        _members = d.pop("members", UNSET)
        members: list[ObjectAclEntry] | Unset = UNSET
        if _members is not UNSET:
            members = []
            for members_item_data in _members:
                members_item = ObjectAclEntry.from_dict(members_item_data)

                members.append(members_item)

        def _parse_workspace_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        workspace_id = _parse_workspace_id(d.pop("workspaceId", UNSET))

        hypothesis_set_ids = cast(list[str], d.pop("hypothesisSetIds", UNSET))

        question_ids = cast(list[str], d.pop("questionIds", UNSET))

        chronicle_ids = cast(list[str], d.pop("chronicleIds", UNSET))

        report_ids = cast(list[str], d.pop("reportIds", UNSET))

        investigation = cls(
            id=id,
            display_id=display_id,
            title=title,
            status=status,
            created_by=created_by,
            created_at=created_at,
            updated_at=updated_at,
            summary=summary,
            tags=tags,
            item_count=item_count,
            items=items,
            owner=owner,
            visibility=visibility,
            tlp=tlp,
            members=members,
            workspace_id=workspace_id,
            hypothesis_set_ids=hypothesis_set_ids,
            question_ids=question_ids,
            chronicle_ids=chronicle_ids,
            report_ids=report_ids,
        )

        investigation.additional_properties = d
        return investigation

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
