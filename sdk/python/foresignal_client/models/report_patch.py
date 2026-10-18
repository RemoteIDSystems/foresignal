from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.report_patch_status import ReportPatchStatus
from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="ReportPatch")


@_attrs_define
class ReportPatch:
    """JSON merge patch for a report draft (editors); `version` is the optimistic lock

    Attributes:
        title (str | Unset):
        subtitle (None | str | Unset):
        status (ReportPatchStatus | Unset):
        authors (list[str] | Unset):
        tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
        visibility (Visibility | Unset):
        time_window (None | TimeRange | Unset):
        area (Geometry | None | Unset):
        block_order (list[str] | Unset): Reorder blocks by id
        version (int | Unset):
    """

    title: str | Unset = UNSET
    subtitle: None | str | Unset = UNSET
    status: ReportPatchStatus | Unset = UNSET
    authors: list[str] | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    visibility: Visibility | Unset = UNSET
    time_window: None | TimeRange | Unset = UNSET
    area: Geometry | None | Unset = UNSET
    block_order: list[str] | Unset = UNSET
    version: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry
        from ..models.time_range import TimeRange

        title = self.title

        subtitle: None | str | Unset
        if isinstance(self.subtitle, Unset):
            subtitle = UNSET
        else:
            subtitle = self.subtitle

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        authors: list[str] | Unset = UNSET
        if not isinstance(self.authors, Unset):
            authors = self.authors

        tlp: str | Unset = UNSET
        if not isinstance(self.tlp, Unset):
            tlp = self.tlp.value

        visibility: str | Unset = UNSET
        if not isinstance(self.visibility, Unset):
            visibility = self.visibility.value

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

        block_order: list[str] | Unset = UNSET
        if not isinstance(self.block_order, Unset):
            block_order = self.block_order

        version = self.version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if title is not UNSET:
            field_dict["title"] = title
        if subtitle is not UNSET:
            field_dict["subtitle"] = subtitle
        if status is not UNSET:
            field_dict["status"] = status
        if authors is not UNSET:
            field_dict["authors"] = authors
        if tlp is not UNSET:
            field_dict["tlp"] = tlp
        if visibility is not UNSET:
            field_dict["visibility"] = visibility
        if time_window is not UNSET:
            field_dict["timeWindow"] = time_window
        if area is not UNSET:
            field_dict["area"] = area
        if block_order is not UNSET:
            field_dict["blockOrder"] = block_order
        if version is not UNSET:
            field_dict["version"] = version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.time_range import TimeRange

        d = dict(src_dict)
        title = d.pop("title", UNSET)

        def _parse_subtitle(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        subtitle = _parse_subtitle(d.pop("subtitle", UNSET))

        _status = d.pop("status", UNSET)
        status: ReportPatchStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = ReportPatchStatus(_status)

        authors = cast(list[str], d.pop("authors", UNSET))

        _tlp = d.pop("tlp", UNSET)
        tlp: Tlp | Unset
        if isinstance(_tlp, Unset):
            tlp = UNSET
        else:
            tlp = Tlp(_tlp)

        _visibility = d.pop("visibility", UNSET)
        visibility: Visibility | Unset
        if isinstance(_visibility, Unset):
            visibility = UNSET
        else:
            visibility = Visibility(_visibility)

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

        block_order = cast(list[str], d.pop("blockOrder", UNSET))

        version = d.pop("version", UNSET)

        report_patch = cls(
            title=title,
            subtitle=subtitle,
            status=status,
            authors=authors,
            tlp=tlp,
            visibility=visibility,
            time_window=time_window,
            area=area,
            block_order=block_order,
            version=version,
        )

        report_patch.additional_properties = d
        return report_patch

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
