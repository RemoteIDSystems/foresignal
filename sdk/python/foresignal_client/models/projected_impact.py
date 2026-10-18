from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.finding_severity import FindingSeverity
from ..models.projected_impact_status import ProjectedImpactStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.hazard_forecast import HazardForecast
    from ..models.projected_impact_dependency_paths_item import (
        ProjectedImpactDependencyPathsItem,
    )
    from ..models.projected_impact_exposed_assets_item import (
        ProjectedImpactExposedAssetsItem,
    )
    from ..models.projected_impact_inputs_item import ProjectedImpactInputsItem
    from ..models.projected_impact_interval import ProjectedImpactInterval
    from ..models.projected_impact_question_window import ProjectedImpactQuestionWindow
    from ..models.projected_impact_skipped_item import ProjectedImpactSkippedItem


T = TypeVar("T", bound="ProjectedImpact")


@_attrs_define
class ProjectedImpact:
    """A `projected-impact` finding: P(impact) = P(hazard at asset) × P(impact | hazard class)

    Attributes:
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        hazard (HazardForecast): A hazard forecast adapted from an ingested record, with its published probability model
        impact_family (str):  Example: outage.
        admin1 (str):  Example: US-TX.
        p_hazard (float):
        p_conditional (float):
        p_impact (float):
        interval (ProjectedImpactInterval): Interval bounds
        n (int):
        labels (list[str]): Always includes "modelled; heuristic links may be wrong; not an official warning"
        status (ProjectedImpactStatus):
        detected_at (datetime.datetime):
        ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        region_class (None | str | Unset):
        severity (FindingSeverity | Unset):
        exposed_assets (list[ProjectedImpactExposedAssetsItem] | Unset):
        dependency_paths (list[ProjectedImpactDependencyPathsItem] | Unset):
        inputs (list[ProjectedImpactInputsItem] | Unset):
        skipped (list[ProjectedImpactSkippedItem] | Unset):
        question_id (None | str | Unset):
        superseded_by (None | str | Unset):
        manifest_id (None | str | Unset):
        version (str | Unset):  Example: impact-projection 1.0.0.
        caveats (list[Caveat] | Unset):
        admin_1_name (None | str | Unset):  Example: Texas.
        lag_hours (int | Unset): Hours after the hazard within which the impact counts (the pair's lag)
        question_window (ProjectedImpactQuestionWindow | Unset): The ledger question's window: the hazard's valid start
            to its valid end plus the lag
    """

    finding_id: str
    hazard: HazardForecast
    impact_family: str
    admin1: str
    p_hazard: float
    p_conditional: float
    p_impact: float
    interval: ProjectedImpactInterval
    n: int
    labels: list[str]
    status: ProjectedImpactStatus
    detected_at: datetime.datetime
    ref: str | Unset = UNSET
    region_class: None | str | Unset = UNSET
    severity: FindingSeverity | Unset = UNSET
    exposed_assets: list[ProjectedImpactExposedAssetsItem] | Unset = UNSET
    dependency_paths: list[ProjectedImpactDependencyPathsItem] | Unset = UNSET
    inputs: list[ProjectedImpactInputsItem] | Unset = UNSET
    skipped: list[ProjectedImpactSkippedItem] | Unset = UNSET
    question_id: None | str | Unset = UNSET
    superseded_by: None | str | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    version: str | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    admin_1_name: None | str | Unset = UNSET
    lag_hours: int | Unset = UNSET
    question_window: ProjectedImpactQuestionWindow | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        finding_id = self.finding_id

        hazard = self.hazard.to_dict()

        impact_family = self.impact_family

        admin1 = self.admin1

        p_hazard = self.p_hazard

        p_conditional = self.p_conditional

        p_impact = self.p_impact

        interval = self.interval.to_dict()

        n = self.n

        labels = self.labels

        status = self.status.value

        detected_at = self.detected_at.isoformat()

        ref = self.ref

        region_class: None | str | Unset
        if isinstance(self.region_class, Unset):
            region_class = UNSET
        else:
            region_class = self.region_class

        severity: str | Unset = UNSET
        if not isinstance(self.severity, Unset):
            severity = self.severity.value

        exposed_assets: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.exposed_assets, Unset):
            exposed_assets = []
            for exposed_assets_item_data in self.exposed_assets:
                exposed_assets_item = exposed_assets_item_data.to_dict()
                exposed_assets.append(exposed_assets_item)

        dependency_paths: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.dependency_paths, Unset):
            dependency_paths = []
            for dependency_paths_item_data in self.dependency_paths:
                dependency_paths_item = dependency_paths_item_data.to_dict()
                dependency_paths.append(dependency_paths_item)

        inputs: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.inputs, Unset):
            inputs = []
            for inputs_item_data in self.inputs:
                inputs_item = inputs_item_data.to_dict()
                inputs.append(inputs_item)

        skipped: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.skipped, Unset):
            skipped = []
            for skipped_item_data in self.skipped:
                skipped_item = skipped_item_data.to_dict()
                skipped.append(skipped_item)

        question_id: None | str | Unset
        if isinstance(self.question_id, Unset):
            question_id = UNSET
        else:
            question_id = self.question_id

        superseded_by: None | str | Unset
        if isinstance(self.superseded_by, Unset):
            superseded_by = UNSET
        else:
            superseded_by = self.superseded_by

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        version = self.version

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        admin_1_name: None | str | Unset
        if isinstance(self.admin_1_name, Unset):
            admin_1_name = UNSET
        else:
            admin_1_name = self.admin_1_name

        lag_hours = self.lag_hours

        question_window: dict[str, Any] | Unset = UNSET
        if not isinstance(self.question_window, Unset):
            question_window = self.question_window.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "findingId": finding_id,
                "hazard": hazard,
                "impactFamily": impact_family,
                "admin1": admin1,
                "pHazard": p_hazard,
                "pConditional": p_conditional,
                "pImpact": p_impact,
                "interval": interval,
                "n": n,
                "labels": labels,
                "status": status,
                "detectedAt": detected_at,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if region_class is not UNSET:
            field_dict["regionClass"] = region_class
        if severity is not UNSET:
            field_dict["severity"] = severity
        if exposed_assets is not UNSET:
            field_dict["exposedAssets"] = exposed_assets
        if dependency_paths is not UNSET:
            field_dict["dependencyPaths"] = dependency_paths
        if inputs is not UNSET:
            field_dict["inputs"] = inputs
        if skipped is not UNSET:
            field_dict["skipped"] = skipped
        if question_id is not UNSET:
            field_dict["questionId"] = question_id
        if superseded_by is not UNSET:
            field_dict["supersededBy"] = superseded_by
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if version is not UNSET:
            field_dict["version"] = version
        if caveats is not UNSET:
            field_dict["caveats"] = caveats
        if admin_1_name is not UNSET:
            field_dict["admin1Name"] = admin_1_name
        if lag_hours is not UNSET:
            field_dict["lagHours"] = lag_hours
        if question_window is not UNSET:
            field_dict["questionWindow"] = question_window

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.hazard_forecast import HazardForecast
        from ..models.projected_impact_dependency_paths_item import (
            ProjectedImpactDependencyPathsItem,
        )
        from ..models.projected_impact_exposed_assets_item import (
            ProjectedImpactExposedAssetsItem,
        )
        from ..models.projected_impact_inputs_item import ProjectedImpactInputsItem
        from ..models.projected_impact_interval import ProjectedImpactInterval
        from ..models.projected_impact_question_window import (
            ProjectedImpactQuestionWindow,
        )
        from ..models.projected_impact_skipped_item import ProjectedImpactSkippedItem

        d = dict(src_dict)
        finding_id = d.pop("findingId")

        hazard = HazardForecast.from_dict(d.pop("hazard"))

        impact_family = d.pop("impactFamily")

        admin1 = d.pop("admin1")

        p_hazard = d.pop("pHazard")

        p_conditional = d.pop("pConditional")

        p_impact = d.pop("pImpact")

        interval = ProjectedImpactInterval.from_dict(d.pop("interval"))

        n = d.pop("n")

        labels = cast(list[str], d.pop("labels"))

        status = ProjectedImpactStatus(d.pop("status"))

        detected_at = datetime.datetime.fromisoformat(d.pop("detectedAt"))

        ref = d.pop("ref", UNSET)

        def _parse_region_class(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region_class = _parse_region_class(d.pop("regionClass", UNSET))

        _severity = d.pop("severity", UNSET)
        severity: FindingSeverity | Unset
        if isinstance(_severity, Unset):
            severity = UNSET
        else:
            severity = FindingSeverity(_severity)

        _exposed_assets = d.pop("exposedAssets", UNSET)
        exposed_assets: list[ProjectedImpactExposedAssetsItem] | Unset = UNSET
        if _exposed_assets is not UNSET:
            exposed_assets = []
            for exposed_assets_item_data in _exposed_assets:
                exposed_assets_item = ProjectedImpactExposedAssetsItem.from_dict(
                    exposed_assets_item_data
                )

                exposed_assets.append(exposed_assets_item)

        _dependency_paths = d.pop("dependencyPaths", UNSET)
        dependency_paths: list[ProjectedImpactDependencyPathsItem] | Unset = UNSET
        if _dependency_paths is not UNSET:
            dependency_paths = []
            for dependency_paths_item_data in _dependency_paths:
                dependency_paths_item = ProjectedImpactDependencyPathsItem.from_dict(
                    dependency_paths_item_data
                )

                dependency_paths.append(dependency_paths_item)

        _inputs = d.pop("inputs", UNSET)
        inputs: list[ProjectedImpactInputsItem] | Unset = UNSET
        if _inputs is not UNSET:
            inputs = []
            for inputs_item_data in _inputs:
                inputs_item = ProjectedImpactInputsItem.from_dict(inputs_item_data)

                inputs.append(inputs_item)

        _skipped = d.pop("skipped", UNSET)
        skipped: list[ProjectedImpactSkippedItem] | Unset = UNSET
        if _skipped is not UNSET:
            skipped = []
            for skipped_item_data in _skipped:
                skipped_item = ProjectedImpactSkippedItem.from_dict(skipped_item_data)

                skipped.append(skipped_item)

        def _parse_question_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        question_id = _parse_question_id(d.pop("questionId", UNSET))

        def _parse_superseded_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        superseded_by = _parse_superseded_by(d.pop("supersededBy", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        version = d.pop("version", UNSET)

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        def _parse_admin_1_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        admin_1_name = _parse_admin_1_name(d.pop("admin1Name", UNSET))

        lag_hours = d.pop("lagHours", UNSET)

        _question_window = d.pop("questionWindow", UNSET)
        question_window: ProjectedImpactQuestionWindow | Unset
        if isinstance(_question_window, Unset):
            question_window = UNSET
        else:
            question_window = ProjectedImpactQuestionWindow.from_dict(_question_window)

        projected_impact = cls(
            finding_id=finding_id,
            hazard=hazard,
            impact_family=impact_family,
            admin1=admin1,
            p_hazard=p_hazard,
            p_conditional=p_conditional,
            p_impact=p_impact,
            interval=interval,
            n=n,
            labels=labels,
            status=status,
            detected_at=detected_at,
            ref=ref,
            region_class=region_class,
            severity=severity,
            exposed_assets=exposed_assets,
            dependency_paths=dependency_paths,
            inputs=inputs,
            skipped=skipped,
            question_id=question_id,
            superseded_by=superseded_by,
            manifest_id=manifest_id,
            version=version,
            caveats=caveats,
            admin_1_name=admin_1_name,
            lag_hours=lag_hours,
            question_window=question_window,
        )

        projected_impact.additional_properties = d
        return projected_impact

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
