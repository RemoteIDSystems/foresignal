from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.dossier_sections_analysis import DossierSectionsAnalysis
    from ..models.dossier_sections_forecast import DossierSectionsForecast
    from ..models.dossier_sections_imagery import DossierSectionsImagery
    from ..models.dossier_sections_impacts import DossierSectionsImpacts
    from ..models.dossier_sections_overview import DossierSectionsOverview
    from ..models.dossier_sections_related import DossierSectionsRelated
    from ..models.dossier_sections_timeline import DossierSectionsTimeline


T = TypeVar("T", bound="DossierSections")


@_attrs_define
class DossierSections:
    """Requested sections; each renders independently by `status`

    Attributes:
        overview (DossierSectionsOverview | Unset): Type-specific key fields
        imagery (DossierSectionsImagery | Unset): Imagery intersecting the object (≤ 12, newest first)
        forecast (DossierSectionsForecast | Unset): Official forecasts and ledger questions about the object
        impacts (DossierSectionsImpacts | Unset): 127 projected impacts for the object
        related (DossierSectionsRelated | Unset): Relations grouped for panels
        timeline (DossierSectionsTimeline | Unset): History newest first (≤ 50)
        analysis (DossierSectionsAnalysis | Unset): Analytic objects that reference this object
    """

    overview: DossierSectionsOverview | Unset = UNSET
    imagery: DossierSectionsImagery | Unset = UNSET
    forecast: DossierSectionsForecast | Unset = UNSET
    impacts: DossierSectionsImpacts | Unset = UNSET
    related: DossierSectionsRelated | Unset = UNSET
    timeline: DossierSectionsTimeline | Unset = UNSET
    analysis: DossierSectionsAnalysis | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        overview: dict[str, Any] | Unset = UNSET
        if not isinstance(self.overview, Unset):
            overview = self.overview.to_dict()

        imagery: dict[str, Any] | Unset = UNSET
        if not isinstance(self.imagery, Unset):
            imagery = self.imagery.to_dict()

        forecast: dict[str, Any] | Unset = UNSET
        if not isinstance(self.forecast, Unset):
            forecast = self.forecast.to_dict()

        impacts: dict[str, Any] | Unset = UNSET
        if not isinstance(self.impacts, Unset):
            impacts = self.impacts.to_dict()

        related: dict[str, Any] | Unset = UNSET
        if not isinstance(self.related, Unset):
            related = self.related.to_dict()

        timeline: dict[str, Any] | Unset = UNSET
        if not isinstance(self.timeline, Unset):
            timeline = self.timeline.to_dict()

        analysis: dict[str, Any] | Unset = UNSET
        if not isinstance(self.analysis, Unset):
            analysis = self.analysis.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if overview is not UNSET:
            field_dict["overview"] = overview
        if imagery is not UNSET:
            field_dict["imagery"] = imagery
        if forecast is not UNSET:
            field_dict["forecast"] = forecast
        if impacts is not UNSET:
            field_dict["impacts"] = impacts
        if related is not UNSET:
            field_dict["related"] = related
        if timeline is not UNSET:
            field_dict["timeline"] = timeline
        if analysis is not UNSET:
            field_dict["analysis"] = analysis

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.dossier_sections_analysis import DossierSectionsAnalysis
        from ..models.dossier_sections_forecast import DossierSectionsForecast
        from ..models.dossier_sections_imagery import DossierSectionsImagery
        from ..models.dossier_sections_impacts import DossierSectionsImpacts
        from ..models.dossier_sections_overview import DossierSectionsOverview
        from ..models.dossier_sections_related import DossierSectionsRelated
        from ..models.dossier_sections_timeline import DossierSectionsTimeline

        d = dict(src_dict)
        _overview = d.pop("overview", UNSET)
        overview: DossierSectionsOverview | Unset
        if isinstance(_overview, Unset):
            overview = UNSET
        else:
            overview = DossierSectionsOverview.from_dict(_overview)

        _imagery = d.pop("imagery", UNSET)
        imagery: DossierSectionsImagery | Unset
        if isinstance(_imagery, Unset):
            imagery = UNSET
        else:
            imagery = DossierSectionsImagery.from_dict(_imagery)

        _forecast = d.pop("forecast", UNSET)
        forecast: DossierSectionsForecast | Unset
        if isinstance(_forecast, Unset):
            forecast = UNSET
        else:
            forecast = DossierSectionsForecast.from_dict(_forecast)

        _impacts = d.pop("impacts", UNSET)
        impacts: DossierSectionsImpacts | Unset
        if isinstance(_impacts, Unset):
            impacts = UNSET
        else:
            impacts = DossierSectionsImpacts.from_dict(_impacts)

        _related = d.pop("related", UNSET)
        related: DossierSectionsRelated | Unset
        if isinstance(_related, Unset):
            related = UNSET
        else:
            related = DossierSectionsRelated.from_dict(_related)

        _timeline = d.pop("timeline", UNSET)
        timeline: DossierSectionsTimeline | Unset
        if isinstance(_timeline, Unset):
            timeline = UNSET
        else:
            timeline = DossierSectionsTimeline.from_dict(_timeline)

        _analysis = d.pop("analysis", UNSET)
        analysis: DossierSectionsAnalysis | Unset
        if isinstance(_analysis, Unset):
            analysis = UNSET
        else:
            analysis = DossierSectionsAnalysis.from_dict(_analysis)

        dossier_sections = cls(
            overview=overview,
            imagery=imagery,
            forecast=forecast,
            impacts=impacts,
            related=related,
            timeline=timeline,
            analysis=analysis,
        )

        dossier_sections.additional_properties = d
        return dossier_sections

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
