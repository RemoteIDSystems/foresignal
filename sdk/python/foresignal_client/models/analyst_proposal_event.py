from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analyst_proposal_event_change import AnalystProposalEventChange

if TYPE_CHECKING:
    from ..models.analyst_proposal import AnalystProposal


T = TypeVar("T", bound="AnalystProposalEvent")


@_attrs_define
class AnalystProposalEvent:
    """SSE payload `analyst.proposal` on topic `analyst`

    Attributes:
        change (AnalystProposalEventChange):
        proposal (AnalystProposal): A proposed action from an analyst run; executes only on confirmation or under
            allowActions with an agent-scoped owner
    """

    change: AnalystProposalEventChange
    proposal: AnalystProposal
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        change = self.change.value

        proposal = self.proposal.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "change": change,
                "proposal": proposal,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analyst_proposal import AnalystProposal

        d = dict(src_dict)
        change = AnalystProposalEventChange(d.pop("change"))

        proposal = AnalystProposal.from_dict(d.pop("proposal"))

        analyst_proposal_event = cls(
            change=change,
            proposal=proposal,
        )

        analyst_proposal_event.additional_properties = d
        return analyst_proposal_event

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
