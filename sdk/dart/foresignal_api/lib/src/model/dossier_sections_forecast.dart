//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/dossier_sections_overview_facets.dart';
import 'package:foresignal_api/src/model/dossier_sections_overview_links.dart';
import 'package:foresignal_api/src/model/ledger_question_brief.dart';
import 'package:foresignal_api/src/model/dossier_sections_forecast_official_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_sections_forecast.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierSectionsForecast {
  /// Returns a new [DossierSectionsForecast] instance.
  DossierSectionsForecast({

    required  this.status,

     this.message,

     this.links,

     this.facets,

     this.truncated,

     this.official,

     this.questions,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final DossierSectionsForecastStatusEnum status;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final DossierSectionsOverviewLinks? links;



  @JsonKey(
    
    name: r'facets',
    required: false,
    includeIfNull: false,
  )


  final DossierSectionsOverviewFacets? facets;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



  @JsonKey(
    
    name: r'official',
    required: false,
    includeIfNull: false,
  )


  final List<DossierSectionsForecastOfficialInner>? official;



  @JsonKey(
    
    name: r'questions',
    required: false,
    includeIfNull: false,
  )


  final List<LedgerQuestionBrief>? questions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierSectionsForecast &&
      other.status == status &&
      other.message == message &&
      other.links == links &&
      other.facets == facets &&
      other.truncated == truncated &&
      other.official == official &&
      other.questions == questions;

    @override
    int get hashCode =>
        status.hashCode +
        (message == null ? 0 : message.hashCode) +
        (links == null ? 0 : links.hashCode) +
        (facets == null ? 0 : facets.hashCode) +
        truncated.hashCode +
        official.hashCode +
        questions.hashCode;

  factory DossierSectionsForecast.fromJson(Map<String, dynamic> json) => _$DossierSectionsForecastFromJson(json);

  Map<String, dynamic> toJson() => _$DossierSectionsForecastToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DossierSectionsForecastStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'empty')
empty(r'empty'),
@JsonValue(r'unavailable')
unavailable(r'unavailable'),
@JsonValue(r'not-installed')
notInstalled(r'not-installed');

const DossierSectionsForecastStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


