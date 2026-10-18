from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.detector_ref import DetectorRef


T = TypeVar("T", bound="SituationInference")


@_attrs_define
class SituationInference:
    """Inference by named, versioned components: status, severity and the deterministic summary

    Attributes:
        summary (str):
        status_basis (str): Why the status is what it is, e.g. `3 signal families, 2 independent evidence groups`
        severity_basis (str): Max core-member severity, or the override and who set it
        summary_template_version (str | Unset):
        linker (DetectorRef | Unset):
        manifest_id (None | str | Unset):
    """

    summary: str
    status_basis: str
    severity_basis: str
    summary_template_version: str | Unset = UNSET
    linker: DetectorRef | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        summary = self.summary

        status_basis = self.status_basis

        severity_basis = self.severity_basis

        summary_template_version = self.summary_template_version

        linker: dict[str, Any] | Unset = UNSET
        if not isinstance(self.linker, Unset):
            linker = self.linker.to_dict()

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "summary": summary,
                "statusBasis": status_basis,
                "severityBasis": severity_basis,
            }
        )
        if summary_template_version is not UNSET:
            field_dict["summaryTemplateVersion"] = summary_template_version
        if linker is not UNSET:
            field_dict["linker"] = linker
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.detector_ref import DetectorRef

        d = dict(src_dict)
        summary = d.pop("summary")

        status_basis = d.pop("statusBasis")

        severity_basis = d.pop("severityBasis")

        summary_template_version = d.pop("summaryTemplateVersion", UNSET)

        _linker = d.pop("linker", UNSET)
        linker: DetectorRef | Unset
        if isinstance(_linker, Unset):
            linker = UNSET
        else:
            linker = DetectorRef.from_dict(_linker)

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        situation_inference = cls(
            summary=summary,
            status_basis=status_basis,
            severity_basis=severity_basis,
            summary_template_version=summary_template_version,
            linker=linker,
            manifest_id=manifest_id,
        )

        situation_inference.additional_properties = d
        return situation_inference

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
