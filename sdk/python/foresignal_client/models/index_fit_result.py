from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.index_fit_result_status import IndexFitResultStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.index_fit_result_components_item import IndexFitResultComponentsItem
    from ..models.index_fit_result_proposed_weights_type_0 import (
        IndexFitResultProposedWeightsType0,
    )
    from ..models.index_fit_result_skill_type_0 import IndexFitResultSkillType0


T = TypeVar("T", bound="IndexFitResult")


@_attrs_define
class IndexFitResult:
    """Monthly or on-demand weight fit; weights are proposed, not applied

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        index_id (str):
        status (IndexFitResultStatus):
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        method (str | Unset):  Default: 'L2-regularised NB regression, rolling origin'.
        lambda_ (float | None | Unset):
        regions (int | None | Unset):
        origins (int | None | Unset):
        proposed_weights (IndexFitResultProposedWeightsType0 | None | Unset):
        weights_version (int | None | Unset): Version to pass to the weights operation
        skill (IndexFitResultSkillType0 | None | Unset):
        components (list[IndexFitResultComponentsItem] | Unset):
        applied (bool | Unset):
        applied_at (datetime.datetime | None | Unset):
        manifest_id (None | str | Unset):
        error (None | str | Unset):
    """

    id: str
    index_id: str
    status: IndexFitResultStatus
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    method: str | Unset = "L2-regularised NB regression, rolling origin"
    lambda_: float | None | Unset = UNSET
    regions: int | None | Unset = UNSET
    origins: int | None | Unset = UNSET
    proposed_weights: IndexFitResultProposedWeightsType0 | None | Unset = UNSET
    weights_version: int | None | Unset = UNSET
    skill: IndexFitResultSkillType0 | None | Unset = UNSET
    components: list[IndexFitResultComponentsItem] | Unset = UNSET
    applied: bool | Unset = UNSET
    applied_at: datetime.datetime | None | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.index_fit_result_proposed_weights_type_0 import (
            IndexFitResultProposedWeightsType0,
        )
        from ..models.index_fit_result_skill_type_0 import IndexFitResultSkillType0

        id = self.id

        index_id = self.index_id

        status = self.status.value

        started_at: None | str | Unset
        if isinstance(self.started_at, Unset):
            started_at = UNSET
        elif isinstance(self.started_at, datetime.datetime):
            started_at = self.started_at.isoformat()
        else:
            started_at = self.started_at

        finished_at: None | str | Unset
        if isinstance(self.finished_at, Unset):
            finished_at = UNSET
        elif isinstance(self.finished_at, datetime.datetime):
            finished_at = self.finished_at.isoformat()
        else:
            finished_at = self.finished_at

        method = self.method

        lambda_: float | None | Unset
        if isinstance(self.lambda_, Unset):
            lambda_ = UNSET
        else:
            lambda_ = self.lambda_

        regions: int | None | Unset
        if isinstance(self.regions, Unset):
            regions = UNSET
        else:
            regions = self.regions

        origins: int | None | Unset
        if isinstance(self.origins, Unset):
            origins = UNSET
        else:
            origins = self.origins

        proposed_weights: dict[str, Any] | None | Unset
        if isinstance(self.proposed_weights, Unset):
            proposed_weights = UNSET
        elif isinstance(self.proposed_weights, IndexFitResultProposedWeightsType0):
            proposed_weights = self.proposed_weights.to_dict()
        else:
            proposed_weights = self.proposed_weights

        weights_version: int | None | Unset
        if isinstance(self.weights_version, Unset):
            weights_version = UNSET
        else:
            weights_version = self.weights_version

        skill: dict[str, Any] | None | Unset
        if isinstance(self.skill, Unset):
            skill = UNSET
        elif isinstance(self.skill, IndexFitResultSkillType0):
            skill = self.skill.to_dict()
        else:
            skill = self.skill

        components: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.components, Unset):
            components = []
            for components_item_data in self.components:
                components_item = components_item_data.to_dict()
                components.append(components_item)

        applied = self.applied

        applied_at: None | str | Unset
        if isinstance(self.applied_at, Unset):
            applied_at = UNSET
        elif isinstance(self.applied_at, datetime.datetime):
            applied_at = self.applied_at.isoformat()
        else:
            applied_at = self.applied_at

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "indexId": index_id,
                "status": status,
            }
        )
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if method is not UNSET:
            field_dict["method"] = method
        if lambda_ is not UNSET:
            field_dict["lambda"] = lambda_
        if regions is not UNSET:
            field_dict["regions"] = regions
        if origins is not UNSET:
            field_dict["origins"] = origins
        if proposed_weights is not UNSET:
            field_dict["proposedWeights"] = proposed_weights
        if weights_version is not UNSET:
            field_dict["weightsVersion"] = weights_version
        if skill is not UNSET:
            field_dict["skill"] = skill
        if components is not UNSET:
            field_dict["components"] = components
        if applied is not UNSET:
            field_dict["applied"] = applied
        if applied_at is not UNSET:
            field_dict["appliedAt"] = applied_at
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.index_fit_result_components_item import (
            IndexFitResultComponentsItem,
        )
        from ..models.index_fit_result_proposed_weights_type_0 import (
            IndexFitResultProposedWeightsType0,
        )
        from ..models.index_fit_result_skill_type_0 import IndexFitResultSkillType0

        d = dict(src_dict)
        id = d.pop("id")

        index_id = d.pop("indexId")

        status = IndexFitResultStatus(d.pop("status"))

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

        def _parse_finished_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                finished_at_type_0 = datetime.datetime.fromisoformat(data)

                return finished_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        finished_at = _parse_finished_at(d.pop("finishedAt", UNSET))

        method = d.pop("method", UNSET)

        def _parse_lambda_(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lambda_ = _parse_lambda_(d.pop("lambda", UNSET))

        def _parse_regions(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        regions = _parse_regions(d.pop("regions", UNSET))

        def _parse_origins(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        origins = _parse_origins(d.pop("origins", UNSET))

        def _parse_proposed_weights(
            data: object,
        ) -> IndexFitResultProposedWeightsType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                proposed_weights_type_0 = IndexFitResultProposedWeightsType0.from_dict(
                    data
                )

                return proposed_weights_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndexFitResultProposedWeightsType0 | None | Unset, data)

        proposed_weights = _parse_proposed_weights(d.pop("proposedWeights", UNSET))

        def _parse_weights_version(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        weights_version = _parse_weights_version(d.pop("weightsVersion", UNSET))

        def _parse_skill(data: object) -> IndexFitResultSkillType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                skill_type_0 = IndexFitResultSkillType0.from_dict(data)

                return skill_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndexFitResultSkillType0 | None | Unset, data)

        skill = _parse_skill(d.pop("skill", UNSET))

        _components = d.pop("components", UNSET)
        components: list[IndexFitResultComponentsItem] | Unset = UNSET
        if _components is not UNSET:
            components = []
            for components_item_data in _components:
                components_item = IndexFitResultComponentsItem.from_dict(
                    components_item_data
                )

                components.append(components_item)

        applied = d.pop("applied", UNSET)

        def _parse_applied_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                applied_at_type_0 = datetime.datetime.fromisoformat(data)

                return applied_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        applied_at = _parse_applied_at(d.pop("appliedAt", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        index_fit_result = cls(
            id=id,
            index_id=index_id,
            status=status,
            started_at=started_at,
            finished_at=finished_at,
            method=method,
            lambda_=lambda_,
            regions=regions,
            origins=origins,
            proposed_weights=proposed_weights,
            weights_version=weights_version,
            skill=skill,
            components=components,
            applied=applied,
            applied_at=applied_at,
            manifest_id=manifest_id,
            error=error,
        )

        index_fit_result.additional_properties = d
        return index_fit_result

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
