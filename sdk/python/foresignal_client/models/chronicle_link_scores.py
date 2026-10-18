from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_link_scores_area_time import ChronicleLinkScoresAreaTime
    from ..models.chronicle_link_scores_entities import ChronicleLinkScoresEntities
    from ..models.chronicle_link_scores_evidence import ChronicleLinkScoresEvidence
    from ..models.chronicle_link_scores_topics import ChronicleLinkScoresTopics


T = TypeVar("T", bound="ChronicleLinkScores")


@_attrs_define
class ChronicleLinkScores:
    """Per key type overlap

    Attributes:
        evidence (ChronicleLinkScoresEvidence | Unset): Overlap estimate for one key type
        entities (ChronicleLinkScoresEntities | Unset): Overlap estimate for one key type
        area_time (ChronicleLinkScoresAreaTime | Unset): Overlap estimate for one key type
        topics (ChronicleLinkScoresTopics | Unset): Overlap estimate for one key type
    """

    evidence: ChronicleLinkScoresEvidence | Unset = UNSET
    entities: ChronicleLinkScoresEntities | Unset = UNSET
    area_time: ChronicleLinkScoresAreaTime | Unset = UNSET
    topics: ChronicleLinkScoresTopics | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        evidence: dict[str, Any] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = self.evidence.to_dict()

        entities: dict[str, Any] | Unset = UNSET
        if not isinstance(self.entities, Unset):
            entities = self.entities.to_dict()

        area_time: dict[str, Any] | Unset = UNSET
        if not isinstance(self.area_time, Unset):
            area_time = self.area_time.to_dict()

        topics: dict[str, Any] | Unset = UNSET
        if not isinstance(self.topics, Unset):
            topics = self.topics.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if entities is not UNSET:
            field_dict["entities"] = entities
        if area_time is not UNSET:
            field_dict["area-time"] = area_time
        if topics is not UNSET:
            field_dict["topics"] = topics

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_link_scores_area_time import ChronicleLinkScoresAreaTime
        from ..models.chronicle_link_scores_entities import ChronicleLinkScoresEntities
        from ..models.chronicle_link_scores_evidence import ChronicleLinkScoresEvidence
        from ..models.chronicle_link_scores_topics import ChronicleLinkScoresTopics

        d = dict(src_dict)
        _evidence = d.pop("evidence", UNSET)
        evidence: ChronicleLinkScoresEvidence | Unset
        if isinstance(_evidence, Unset):
            evidence = UNSET
        else:
            evidence = ChronicleLinkScoresEvidence.from_dict(_evidence)

        _entities = d.pop("entities", UNSET)
        entities: ChronicleLinkScoresEntities | Unset
        if isinstance(_entities, Unset):
            entities = UNSET
        else:
            entities = ChronicleLinkScoresEntities.from_dict(_entities)

        _area_time = d.pop("area-time", UNSET)
        area_time: ChronicleLinkScoresAreaTime | Unset
        if isinstance(_area_time, Unset):
            area_time = UNSET
        else:
            area_time = ChronicleLinkScoresAreaTime.from_dict(_area_time)

        _topics = d.pop("topics", UNSET)
        topics: ChronicleLinkScoresTopics | Unset
        if isinstance(_topics, Unset):
            topics = UNSET
        else:
            topics = ChronicleLinkScoresTopics.from_dict(_topics)

        chronicle_link_scores = cls(
            evidence=evidence,
            entities=entities,
            area_time=area_time,
            topics=topics,
        )

        chronicle_link_scores.additional_properties = d
        return chronicle_link_scores

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
