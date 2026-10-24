from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.report_draft_request_template import ReportDraftRequestTemplate
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.report_source import ReportSource
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="ReportDraftRequest")


@_attrs_define
class ReportDraftRequest:
    """Build a deterministic draft report from a source

    Attributes:
        source (ReportSource): What a report is built from
        template (ReportDraftRequestTemplate | Unset):  Default: ReportDraftRequestTemplate.CUSTOM.
        title (None | str | Unset):
        time_window (None | TimeRange | Unset):
        area (Geometry | None | Unset):
        narrative (bool | Unset): Add a labeled LLM narrative block (agent scope, budgeted) Default: False.
        assessment_id (None | str | Unset):
    """

    source: ReportSource
    template: ReportDraftRequestTemplate | Unset = ReportDraftRequestTemplate.CUSTOM
    title: None | str | Unset = UNSET
    time_window: None | TimeRange | Unset = UNSET
    area: Geometry | None | Unset = UNSET
    narrative: bool | Unset = False
    assessment_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry
        from ..models.time_range import TimeRange

        source = self.source.to_dict()

        template: str | Unset = UNSET
        if not isinstance(self.template, Unset):
            template = self.template.value

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        time_window: dict[str, Any] | None | Unset
        if isinstance(self.time_window, Unset):
            time_window = UNSET
        elif isinstance(self.time_window, TimeRange):
            time_window = self.time_window.to_dict()
        else:
            time_window = self.time_window

        area: dict[str, Any] | None | Unset
        if isinstance(self.area, Unset):
            area = UNSET
        elif isinstance(self.area, Geometry):
            area = self.area.to_dict()
        else:
            area = self.area

        narrative = self.narrative

        assessment_id: None | str | Unset
        if isinstance(self.assessment_id, Unset):
            assessment_id = UNSET
        else:
            assessment_id = self.assessment_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "source": source,
            }
        )
        if template is not UNSET:
            field_dict["template"] = template
        if title is not UNSET:
            field_dict["title"] = title
        if time_window is not UNSET:
            field_dict["timeWindow"] = time_window
        if area is not UNSET:
            field_dict["area"] = area
        if narrative is not UNSET:
            field_dict["narrative"] = narrative
        if assessment_id is not UNSET:
            field_dict["assessmentId"] = assessment_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.report_source import ReportSource
        from ..models.time_range import TimeRange

        d = dict(src_dict)
        source = ReportSource.from_dict(d.pop("source"))

        _template = d.pop("template", UNSET)
        template: ReportDraftRequestTemplate | Unset
        if isinstance(_template, Unset):
            template = UNSET
        else:
            template = ReportDraftRequestTemplate(_template)

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        def _parse_time_window(data: object) -> None | TimeRange | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                time_window_type_0 = TimeRange.from_dict(data)

                return time_window_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | TimeRange | Unset, data)

        time_window = _parse_time_window(d.pop("timeWindow", UNSET))

        def _parse_area(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                area_type_0 = Geometry.from_dict(data)

                return area_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        area = _parse_area(d.pop("area", UNSET))

        narrative = d.pop("narrative", UNSET)

        def _parse_assessment_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        assessment_id = _parse_assessment_id(d.pop("assessmentId", UNSET))

        report_draft_request = cls(
            source=source,
            template=template,
            title=title,
            time_window=time_window,
            area=area,
            narrative=narrative,
            assessment_id=assessment_id,
        )

        report_draft_request.additional_properties = d
        return report_draft_request

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
