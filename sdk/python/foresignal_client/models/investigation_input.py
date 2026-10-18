from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.investigation_input_status import InvestigationInputStatus
from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

T = TypeVar("T", bound="InvestigationInput")


@_attrs_define
class InvestigationInput:
    """New investigation, owned by the calling principal (130 adds visibility, TLP and links)

    Attributes:
        title (str):
        summary (str | Unset):
        tags (list[str] | Unset):
        status (InvestigationInputStatus | Unset):  Default: InvestigationInputStatus.OPEN.
        visibility (Visibility | Unset):
        tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
        workspace_id (None | str | Unset): Linked 129 workspace (130)
        hypothesis_set_ids (list[str] | Unset): Linked 125 hypothesis sets (130)
        question_ids (list[str] | Unset): Linked 122 forecast questions (130)
        chronicle_ids (list[str] | Unset): Linked chronicles (131)
        report_ids (list[str] | Unset): Linked reports (132)
    """

    title: str
    summary: str | Unset = UNSET
    tags: list[str] | Unset = UNSET
    status: InvestigationInputStatus | Unset = InvestigationInputStatus.OPEN
    visibility: Visibility | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    workspace_id: None | str | Unset = UNSET
    hypothesis_set_ids: list[str] | Unset = UNSET
    question_ids: list[str] | Unset = UNSET
    chronicle_ids: list[str] | Unset = UNSET
    report_ids: list[str] | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        title = self.title

        summary = self.summary

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        visibility: str | Unset = UNSET
        if not isinstance(self.visibility, Unset):
            visibility = self.visibility.value

        tlp: str | Unset = UNSET
        if not isinstance(self.tlp, Unset):
            tlp = self.tlp.value

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

        field_dict.update(
            {
                "title": title,
            }
        )
        if summary is not UNSET:
            field_dict["summary"] = summary
        if tags is not UNSET:
            field_dict["tags"] = tags
        if status is not UNSET:
            field_dict["status"] = status
        if visibility is not UNSET:
            field_dict["visibility"] = visibility
        if tlp is not UNSET:
            field_dict["tlp"] = tlp
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
        title = d.pop("title")

        summary = d.pop("summary", UNSET)

        tags = cast(list[str], d.pop("tags", UNSET))

        _status = d.pop("status", UNSET)
        status: InvestigationInputStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = InvestigationInputStatus(_status)

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

        investigation_input = cls(
            title=title,
            summary=summary,
            tags=tags,
            status=status,
            visibility=visibility,
            tlp=tlp,
            workspace_id=workspace_id,
            hypothesis_set_ids=hypothesis_set_ids,
            question_ids=question_ids,
            chronicle_ids=chronicle_ids,
            report_ids=report_ids,
        )

        return investigation_input
