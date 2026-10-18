from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analysis_manifest_build import AnalysisManifestBuild
    from ..models.analysis_manifest_config import AnalysisManifestConfig
    from ..models.analysis_manifest_inputs import AnalysisManifestInputs
    from ..models.analysis_manifest_model_type_0 import AnalysisManifestModelType0
    from ..models.analysis_manifest_registries import AnalysisManifestRegistries


T = TypeVar("T", bound="AnalysisManifest")


@_attrs_define
class AnalysisManifest:
    """The machinery and inputs that produced an analytic output; identical manifests are stored once (116)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        component (str):
        component_version (str):
        build (AnalysisManifestBuild): Foresignal build
        config (AnalysisManifestConfig):
        config_hash (str):
        registries (AnalysisManifestRegistries): Registry file name → sha256, e.g. signal-families.json,
            originators.json
        inputs (AnalysisManifestInputs): Explicit refs (≤ 10,000) or the query definition plus a hash of the ordered
            refs
        content_hash (str):
        created_at (datetime.datetime):
        as_of_seq (int | None | Unset):
        model (AnalysisManifestModelType0 | None | Unset):
    """

    id: str
    component: str
    component_version: str
    build: AnalysisManifestBuild
    config: AnalysisManifestConfig
    config_hash: str
    registries: AnalysisManifestRegistries
    inputs: AnalysisManifestInputs
    content_hash: str
    created_at: datetime.datetime
    as_of_seq: int | None | Unset = UNSET
    model: AnalysisManifestModelType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.analysis_manifest_model_type_0 import AnalysisManifestModelType0

        id = self.id

        component = self.component

        component_version = self.component_version

        build = self.build.to_dict()

        config = self.config.to_dict()

        config_hash = self.config_hash

        registries = self.registries.to_dict()

        inputs = self.inputs.to_dict()

        content_hash = self.content_hash

        created_at = self.created_at.isoformat()

        as_of_seq: int | None | Unset
        if isinstance(self.as_of_seq, Unset):
            as_of_seq = UNSET
        else:
            as_of_seq = self.as_of_seq

        model: dict[str, Any] | None | Unset
        if isinstance(self.model, Unset):
            model = UNSET
        elif isinstance(self.model, AnalysisManifestModelType0):
            model = self.model.to_dict()
        else:
            model = self.model

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "component": component,
                "componentVersion": component_version,
                "build": build,
                "config": config,
                "configHash": config_hash,
                "registries": registries,
                "inputs": inputs,
                "contentHash": content_hash,
                "createdAt": created_at,
            }
        )
        if as_of_seq is not UNSET:
            field_dict["asOfSeq"] = as_of_seq
        if model is not UNSET:
            field_dict["model"] = model

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analysis_manifest_build import AnalysisManifestBuild
        from ..models.analysis_manifest_config import AnalysisManifestConfig
        from ..models.analysis_manifest_inputs import AnalysisManifestInputs
        from ..models.analysis_manifest_model_type_0 import AnalysisManifestModelType0
        from ..models.analysis_manifest_registries import AnalysisManifestRegistries

        d = dict(src_dict)
        id = d.pop("id")

        component = d.pop("component")

        component_version = d.pop("componentVersion")

        build = AnalysisManifestBuild.from_dict(d.pop("build"))

        config = AnalysisManifestConfig.from_dict(d.pop("config"))

        config_hash = d.pop("configHash")

        registries = AnalysisManifestRegistries.from_dict(d.pop("registries"))

        inputs = AnalysisManifestInputs.from_dict(d.pop("inputs"))

        content_hash = d.pop("contentHash")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_as_of_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        as_of_seq = _parse_as_of_seq(d.pop("asOfSeq", UNSET))

        def _parse_model(data: object) -> AnalysisManifestModelType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                model_type_0 = AnalysisManifestModelType0.from_dict(data)

                return model_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AnalysisManifestModelType0 | None | Unset, data)

        model = _parse_model(d.pop("model", UNSET))

        analysis_manifest = cls(
            id=id,
            component=component,
            component_version=component_version,
            build=build,
            config=config,
            config_hash=config_hash,
            registries=registries,
            inputs=inputs,
            content_hash=content_hash,
            created_at=created_at,
            as_of_seq=as_of_seq,
            model=model,
        )

        analysis_manifest.additional_properties = d
        return analysis_manifest

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
