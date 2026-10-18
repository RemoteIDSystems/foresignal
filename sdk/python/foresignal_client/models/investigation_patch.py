from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.investigation_patch_status import InvestigationPatchStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="InvestigationPatch")


@_attrs_define
class InvestigationPatch:
    """Changes to an investigation (editor); owner, visibility, TLP and grants change through PUT /v1/objects/{ref}/acl

    Attributes:
        title (str | Unset):
        status (InvestigationPatchStatus | Unset):
        summary (None | str | Unset):
        tags (list[str] | Unset):
        workspace_id (None | str | Unset): Linked 129 workspace (130)
        hypothesis_set_ids (list[str] | Unset): Linked 125 hypothesis sets (130)
        question_ids (list[str] | Unset): Linked 122 forecast questions (130)
        chronicle_ids (list[str] | Unset): Linked chronicles (131)
        report_ids (list[str] | Unset): Linked reports (132)
    """

    title: str | Unset = UNSET
    status: InvestigationPatchStatus | Unset = UNSET
    summary: None | str | Unset = UNSET
    tags: list[str] | Unset = UNSET
    workspace_id: None | str | Unset = UNSET
    hypothesis_set_ids: list[str] | Unset = UNSET
    question_ids: list[str] | Unset = UNSET
    chronicle_ids: list[str] | Unset = UNSET
    report_ids: list[str] | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        title = self.title

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        summary: None | str | Unset
        if isinstance(self.summary, Unset):
            summary = UNSET
        else:
            summary = self.summary

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

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

        field_dict.update({})
        if title is not UNSET:
            field_dict["title"] = title
        if status is not UNSET:
            field_dict["status"] = status
        if summary is not UNSET:
            field_dict["summary"] = summary
        if tags is not UNSET:
            field_dict["tags"] = tags
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
        d = dict(src_dict)
        title = d.pop("title", UNSET)

        _status = d.pop("status", UNSET)
        status: InvestigationPatchStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = InvestigationPatchStatus(_status)

        def _parse_summary(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        summary = _parse_summary(d.pop("summary", UNSET))

        tags = cast(list[str], d.pop("tags", UNSET))

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

        investigation_patch = cls(
            title=title,
            status=status,
            summary=summary,
            tags=tags,
            workspace_id=workspace_id,
            hypothesis_set_ids=hypothesis_set_ids,
            question_ids=question_ids,
            chronicle_ids=chronicle_ids,
            report_ids=report_ids,
        )

        return investigation_patch
