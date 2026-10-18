from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.assessment_cadence import AssessmentCadence
from ..models.assessment_effort_type_0 import AssessmentEffortType0
from ..models.assessment_status import AssessmentStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.assessment_coverage_type_0 import AssessmentCoverageType0
    from ..models.assessment_prompt_templates_item import AssessmentPromptTemplatesItem
    from ..models.assessment_roles import AssessmentRoles
    from ..models.assessment_sections_item import AssessmentSectionsItem
    from ..models.key_judgment import KeyJudgment
    from ..models.social_versus_observed import SocialVersusObserved


T = TypeVar("T", bound="Assessment")


@_attrs_define
class Assessment:
    """ICD 203-structured assessment that builds on the previous one of its cadence

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cadence (AssessmentCadence):
        period_start (datetime.datetime):
        period_end (datetime.datetime):
        status (AssessmentStatus):
        bluf (list[KeyJudgment]):
        sections (list[AssessmentSectionsItem]):
        private (bool):
        created_at (datetime.datetime):
        ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cycle_id (None | str | Unset):
        situation_id (None | str | Unset):
        previous_assessment_id (None | str | Unset):
        headline (None | str | Unset): First BLUF judgment or top deterministic change
        markdown (None | str | Unset): Full rendering (omitted in lists)
        html (None | str | Unset): Full rendering (omitted in lists)
        packet_ref (None | str | Unset):
        roles (AssessmentRoles | Unset): Per role
        model (None | str | Unset):
        effort (AssessmentEffortType0 | None | Unset):
        manifest_id (None | str | Unset):
        prompt_templates (list[AssessmentPromptTemplatesItem] | Unset):
        citations (list[str] | Unset):
        lint_warnings (list[str] | Unset):
        footer (str | Unset):  Default: 'Automated analysis. Probabilities use ICD 203 terms; evidence is cited;
            speculation is labelled. Verify before acting.'.
        coverage (AssessmentCoverageType0 | None | Unset):
        social (None | SocialVersusObserved | Unset): "What people are saying versus what we observe" (137)
    """

    id: str
    cadence: AssessmentCadence
    period_start: datetime.datetime
    period_end: datetime.datetime
    status: AssessmentStatus
    bluf: list[KeyJudgment]
    sections: list[AssessmentSectionsItem]
    private: bool
    created_at: datetime.datetime
    ref: str | Unset = UNSET
    cycle_id: None | str | Unset = UNSET
    situation_id: None | str | Unset = UNSET
    previous_assessment_id: None | str | Unset = UNSET
    headline: None | str | Unset = UNSET
    markdown: None | str | Unset = UNSET
    html: None | str | Unset = UNSET
    packet_ref: None | str | Unset = UNSET
    roles: AssessmentRoles | Unset = UNSET
    model: None | str | Unset = UNSET
    effort: AssessmentEffortType0 | None | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    prompt_templates: list[AssessmentPromptTemplatesItem] | Unset = UNSET
    citations: list[str] | Unset = UNSET
    lint_warnings: list[str] | Unset = UNSET
    footer: str | Unset = (
        "Automated analysis. Probabilities use ICD 203 terms; evidence is cited; speculation is labelled. Verify before acting."
    )
    coverage: AssessmentCoverageType0 | None | Unset = UNSET
    social: None | SocialVersusObserved | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.assessment_coverage_type_0 import AssessmentCoverageType0
        from ..models.social_versus_observed import SocialVersusObserved

        id = self.id

        cadence = self.cadence.value

        period_start = self.period_start.isoformat()

        period_end = self.period_end.isoformat()

        status = self.status.value

        bluf = []
        for bluf_item_data in self.bluf:
            bluf_item = bluf_item_data.to_dict()
            bluf.append(bluf_item)

        sections = []
        for sections_item_data in self.sections:
            sections_item = sections_item_data.to_dict()
            sections.append(sections_item)

        private = self.private

        created_at = self.created_at.isoformat()

        ref = self.ref

        cycle_id: None | str | Unset
        if isinstance(self.cycle_id, Unset):
            cycle_id = UNSET
        else:
            cycle_id = self.cycle_id

        situation_id: None | str | Unset
        if isinstance(self.situation_id, Unset):
            situation_id = UNSET
        else:
            situation_id = self.situation_id

        previous_assessment_id: None | str | Unset
        if isinstance(self.previous_assessment_id, Unset):
            previous_assessment_id = UNSET
        else:
            previous_assessment_id = self.previous_assessment_id

        headline: None | str | Unset
        if isinstance(self.headline, Unset):
            headline = UNSET
        else:
            headline = self.headline

        markdown: None | str | Unset
        if isinstance(self.markdown, Unset):
            markdown = UNSET
        else:
            markdown = self.markdown

        html: None | str | Unset
        if isinstance(self.html, Unset):
            html = UNSET
        else:
            html = self.html

        packet_ref: None | str | Unset
        if isinstance(self.packet_ref, Unset):
            packet_ref = UNSET
        else:
            packet_ref = self.packet_ref

        roles: dict[str, Any] | Unset = UNSET
        if not isinstance(self.roles, Unset):
            roles = self.roles.to_dict()

        model: None | str | Unset
        if isinstance(self.model, Unset):
            model = UNSET
        else:
            model = self.model

        effort: None | str | Unset
        if isinstance(self.effort, Unset):
            effort = UNSET
        elif isinstance(self.effort, AssessmentEffortType0):
            effort = self.effort.value
        else:
            effort = self.effort

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        prompt_templates: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.prompt_templates, Unset):
            prompt_templates = []
            for prompt_templates_item_data in self.prompt_templates:
                prompt_templates_item = prompt_templates_item_data.to_dict()
                prompt_templates.append(prompt_templates_item)

        citations: list[str] | Unset = UNSET
        if not isinstance(self.citations, Unset):
            citations = self.citations

        lint_warnings: list[str] | Unset = UNSET
        if not isinstance(self.lint_warnings, Unset):
            lint_warnings = self.lint_warnings

        footer = self.footer

        coverage: dict[str, Any] | None | Unset
        if isinstance(self.coverage, Unset):
            coverage = UNSET
        elif isinstance(self.coverage, AssessmentCoverageType0):
            coverage = self.coverage.to_dict()
        else:
            coverage = self.coverage

        social: dict[str, Any] | None | Unset
        if isinstance(self.social, Unset):
            social = UNSET
        elif isinstance(self.social, SocialVersusObserved):
            social = self.social.to_dict()
        else:
            social = self.social

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "cadence": cadence,
                "periodStart": period_start,
                "periodEnd": period_end,
                "status": status,
                "bluf": bluf,
                "sections": sections,
                "private": private,
                "createdAt": created_at,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if cycle_id is not UNSET:
            field_dict["cycleId"] = cycle_id
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id
        if previous_assessment_id is not UNSET:
            field_dict["previousAssessmentId"] = previous_assessment_id
        if headline is not UNSET:
            field_dict["headline"] = headline
        if markdown is not UNSET:
            field_dict["markdown"] = markdown
        if html is not UNSET:
            field_dict["html"] = html
        if packet_ref is not UNSET:
            field_dict["packetRef"] = packet_ref
        if roles is not UNSET:
            field_dict["roles"] = roles
        if model is not UNSET:
            field_dict["model"] = model
        if effort is not UNSET:
            field_dict["effort"] = effort
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if prompt_templates is not UNSET:
            field_dict["promptTemplates"] = prompt_templates
        if citations is not UNSET:
            field_dict["citations"] = citations
        if lint_warnings is not UNSET:
            field_dict["lintWarnings"] = lint_warnings
        if footer is not UNSET:
            field_dict["footer"] = footer
        if coverage is not UNSET:
            field_dict["coverage"] = coverage
        if social is not UNSET:
            field_dict["social"] = social

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.assessment_coverage_type_0 import AssessmentCoverageType0
        from ..models.assessment_prompt_templates_item import (
            AssessmentPromptTemplatesItem,
        )
        from ..models.assessment_roles import AssessmentRoles
        from ..models.assessment_sections_item import AssessmentSectionsItem
        from ..models.key_judgment import KeyJudgment
        from ..models.social_versus_observed import SocialVersusObserved

        d = dict(src_dict)
        id = d.pop("id")

        cadence = AssessmentCadence(d.pop("cadence"))

        period_start = datetime.datetime.fromisoformat(d.pop("periodStart"))

        period_end = datetime.datetime.fromisoformat(d.pop("periodEnd"))

        status = AssessmentStatus(d.pop("status"))

        bluf = []
        _bluf = d.pop("bluf")
        for bluf_item_data in _bluf:
            bluf_item = KeyJudgment.from_dict(bluf_item_data)

            bluf.append(bluf_item)

        sections = []
        _sections = d.pop("sections")
        for sections_item_data in _sections:
            sections_item = AssessmentSectionsItem.from_dict(sections_item_data)

            sections.append(sections_item)

        private = d.pop("private")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        ref = d.pop("ref", UNSET)

        def _parse_cycle_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        cycle_id = _parse_cycle_id(d.pop("cycleId", UNSET))

        def _parse_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        situation_id = _parse_situation_id(d.pop("situationId", UNSET))

        def _parse_previous_assessment_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        previous_assessment_id = _parse_previous_assessment_id(
            d.pop("previousAssessmentId", UNSET)
        )

        def _parse_headline(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        headline = _parse_headline(d.pop("headline", UNSET))

        def _parse_markdown(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        markdown = _parse_markdown(d.pop("markdown", UNSET))

        def _parse_html(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        html = _parse_html(d.pop("html", UNSET))

        def _parse_packet_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        packet_ref = _parse_packet_ref(d.pop("packetRef", UNSET))

        _roles = d.pop("roles", UNSET)
        roles: AssessmentRoles | Unset
        if isinstance(_roles, Unset):
            roles = UNSET
        else:
            roles = AssessmentRoles.from_dict(_roles)

        def _parse_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        model = _parse_model(d.pop("model", UNSET))

        def _parse_effort(data: object) -> AssessmentEffortType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                effort_type_0 = AssessmentEffortType0(data)

                return effort_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AssessmentEffortType0 | None | Unset, data)

        effort = _parse_effort(d.pop("effort", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        _prompt_templates = d.pop("promptTemplates", UNSET)
        prompt_templates: list[AssessmentPromptTemplatesItem] | Unset = UNSET
        if _prompt_templates is not UNSET:
            prompt_templates = []
            for prompt_templates_item_data in _prompt_templates:
                prompt_templates_item = AssessmentPromptTemplatesItem.from_dict(
                    prompt_templates_item_data
                )

                prompt_templates.append(prompt_templates_item)

        citations = cast(list[str], d.pop("citations", UNSET))

        lint_warnings = cast(list[str], d.pop("lintWarnings", UNSET))

        footer = d.pop("footer", UNSET)

        def _parse_coverage(data: object) -> AssessmentCoverageType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                coverage_type_0 = AssessmentCoverageType0.from_dict(data)

                return coverage_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AssessmentCoverageType0 | None | Unset, data)

        coverage = _parse_coverage(d.pop("coverage", UNSET))

        def _parse_social(data: object) -> None | SocialVersusObserved | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                social_type_0 = SocialVersusObserved.from_dict(data)

                return social_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SocialVersusObserved | Unset, data)

        social = _parse_social(d.pop("social", UNSET))

        assessment = cls(
            id=id,
            cadence=cadence,
            period_start=period_start,
            period_end=period_end,
            status=status,
            bluf=bluf,
            sections=sections,
            private=private,
            created_at=created_at,
            ref=ref,
            cycle_id=cycle_id,
            situation_id=situation_id,
            previous_assessment_id=previous_assessment_id,
            headline=headline,
            markdown=markdown,
            html=html,
            packet_ref=packet_ref,
            roles=roles,
            model=model,
            effort=effort,
            manifest_id=manifest_id,
            prompt_templates=prompt_templates,
            citations=citations,
            lint_warnings=lint_warnings,
            footer=footer,
            coverage=coverage,
            social=social,
        )

        assessment.additional_properties = d
        return assessment

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
