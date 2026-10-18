//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/dossier_sections_overview_facets.dart';
import 'package:foresignal_api/src/model/dossier_sections_overview_links.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_sections_analysis.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierSectionsAnalysis {
  /// Returns a new [DossierSectionsAnalysis] instance.
  DossierSectionsAnalysis({

    required  this.status,

     this.message,

     this.links,

     this.facets,

     this.truncated,

     this.findings,

     this.hypothesisSets,

     this.assessments,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final DossierSectionsAnalysisStatusEnum status;



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
    
    name: r'findings',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectEnvelope>? findings;



  @JsonKey(
    
    name: r'hypothesisSets',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectEnvelope>? hypothesisSets;



  @JsonKey(
    
    name: r'assessments',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectEnvelope>? assessments;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierSectionsAnalysis &&
      other.status == status &&
      other.message == message &&
      other.links == links &&
      other.facets == facets &&
      other.truncated == truncated &&
      other.findings == findings &&
      other.hypothesisSets == hypothesisSets &&
      other.assessments == assessments;

    @override
    int get hashCode =>
        status.hashCode +
        (message == null ? 0 : message.hashCode) +
        (links == null ? 0 : links.hashCode) +
        (facets == null ? 0 : facets.hashCode) +
        truncated.hashCode +
        findings.hashCode +
        hypothesisSets.hashCode +
        assessments.hashCode;

  factory DossierSectionsAnalysis.fromJson(Map<String, dynamic> json) => _$DossierSectionsAnalysisFromJson(json);

  Map<String, dynamic> toJson() => _$DossierSectionsAnalysisToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DossierSectionsAnalysisStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'empty')
empty(r'empty'),
@JsonValue(r'unavailable')
unavailable(r'unavailable'),
@JsonValue(r'not-installed')
notInstalled(r'not-installed');

const DossierSectionsAnalysisStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


