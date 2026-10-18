//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/dossier_sections_forecast.dart';
import 'package:foresignal_api/src/model/dossier_sections_timeline.dart';
import 'package:foresignal_api/src/model/dossier_sections_imagery.dart';
import 'package:foresignal_api/src/model/dossier_sections_analysis.dart';
import 'package:foresignal_api/src/model/dossier_sections_overview.dart';
import 'package:foresignal_api/src/model/dossier_sections_impacts.dart';
import 'package:foresignal_api/src/model/dossier_sections_related.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_sections.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierSections {
  /// Returns a new [DossierSections] instance.
  DossierSections({

     this.overview,

     this.imagery,

     this.forecast,

     this.impacts,

     this.related,

     this.timeline,

     this.analysis,
  });

  @JsonKey(
    
    name: r'overview',
    required: false,
    includeIfNull: false,
  )


  final DossierSectionsOverview? overview;



  @JsonKey(
    
    name: r'imagery',
    required: false,
    includeIfNull: false,
  )


  final DossierSectionsImagery? imagery;



  @JsonKey(
    
    name: r'forecast',
    required: false,
    includeIfNull: false,
  )


  final DossierSectionsForecast? forecast;



  @JsonKey(
    
    name: r'impacts',
    required: false,
    includeIfNull: false,
  )


  final DossierSectionsImpacts? impacts;



  @JsonKey(
    
    name: r'related',
    required: false,
    includeIfNull: false,
  )


  final DossierSectionsRelated? related;



  @JsonKey(
    
    name: r'timeline',
    required: false,
    includeIfNull: false,
  )


  final DossierSectionsTimeline? timeline;



  @JsonKey(
    
    name: r'analysis',
    required: false,
    includeIfNull: false,
  )


  final DossierSectionsAnalysis? analysis;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierSections &&
      other.overview == overview &&
      other.imagery == imagery &&
      other.forecast == forecast &&
      other.impacts == impacts &&
      other.related == related &&
      other.timeline == timeline &&
      other.analysis == analysis;

    @override
    int get hashCode =>
        overview.hashCode +
        imagery.hashCode +
        forecast.hashCode +
        impacts.hashCode +
        related.hashCode +
        timeline.hashCode +
        analysis.hashCode;

  factory DossierSections.fromJson(Map<String, dynamic> json) => _$DossierSectionsFromJson(json);

  Map<String, dynamic> toJson() => _$DossierSectionsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

