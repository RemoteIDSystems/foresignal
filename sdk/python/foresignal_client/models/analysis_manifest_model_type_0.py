from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analysis_manifest_model_type_0_templates_item import (
        AnalysisManifestModelType0TemplatesItem,
    )


T = TypeVar("T", bound="AnalysisManifestModelType0")


@_attrs_define
class AnalysisManifestModelType0:
    """LLM metadata; model output is not reproducible

    Attributes:
        provider (str):
        model (str):
        effort (None | str | Unset):
        prompt_hash (str | Unset):
        tools_hash (None | str | Unset):
        samples (int | None | Unset):
        templates (list[AnalysisManifestModelType0TemplatesItem] | Unset):
    """

    provider: str
    model: str
    effort: None | str | Unset = UNSET
    prompt_hash: str | Unset = UNSET
    tools_hash: None | str | Unset = UNSET
    samples: int | None | Unset = UNSET
    templates: list[AnalysisManifestModelType0TemplatesItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        provider = self.provider

        model = self.model

        effort: None | str | Unset
        if isinstance(self.effort, Unset):
            effort = UNSET
        else:
            effort = self.effort

        prompt_hash = self.prompt_hash

        tools_hash: None | str | Unset
        if isinstance(self.tools_hash, Unset):
            tools_hash = UNSET
        else:
            tools_hash = self.tools_hash

        samples: int | None | Unset
        if isinstance(self.samples, Unset):
            samples = UNSET
        else:
            samples = self.samples

        templates: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.templates, Unset):
            templates = []
            for templates_item_data in self.templates:
                templates_item = templates_item_data.to_dict()
                templates.append(templates_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "provider": provider,
                "model": model,
            }
        )
        if effort is not UNSET:
            field_dict["effort"] = effort
        if prompt_hash is not UNSET:
            field_dict["promptHash"] = prompt_hash
        if tools_hash is not UNSET:
            field_dict["toolsHash"] = tools_hash
        if samples is not UNSET:
            field_dict["samples"] = samples
        if templates is not UNSET:
            field_dict["templates"] = templates

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analysis_manifest_model_type_0_templates_item import (
            AnalysisManifestModelType0TemplatesItem,
        )

        d = dict(src_dict)
        provider = d.pop("provider")

        model = d.pop("model")

        def _parse_effort(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        effort = _parse_effort(d.pop("effort", UNSET))

        prompt_hash = d.pop("promptHash", UNSET)

        def _parse_tools_hash(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        tools_hash = _parse_tools_hash(d.pop("toolsHash", UNSET))

        def _parse_samples(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        samples = _parse_samples(d.pop("samples", UNSET))

        _templates = d.pop("templates", UNSET)
        templates: list[AnalysisManifestModelType0TemplatesItem] | Unset = UNSET
        if _templates is not UNSET:
            templates = []
            for templates_item_data in _templates:
                templates_item = AnalysisManifestModelType0TemplatesItem.from_dict(
                    templates_item_data
                )

                templates.append(templates_item)

        analysis_manifest_model_type_0 = cls(
            provider=provider,
            model=model,
            effort=effort,
            prompt_hash=prompt_hash,
            tools_hash=tools_hash,
            samples=samples,
            templates=templates,
        )

        analysis_manifest_model_type_0.additional_properties = d
        return analysis_manifest_model_type_0

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
