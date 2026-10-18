//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/export_licence_summary_excluded_inner.dart';
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/export_licence_summary_licences_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'export_licence_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExportLicenceSummary {
  /// Returns a new [ExportLicenceSummary] instance.
  ExportLicenceSummary({

    required  this.licences,

    required  this.attributions,

    required  this.personalUseOnly,

     this.personalUseSources,

     this.excluded,
  });

  @JsonKey(
    
    name: r'licences',
    required: true,
    includeIfNull: false,
  )


  final List<ExportLicenceSummaryLicencesInner> licences;



  @JsonKey(
    
    name: r'attributions',
    required: true,
    includeIfNull: false,
  )


  final List<Attribution> attributions;



  @JsonKey(
    
    name: r'personalUseOnly',
    required: true,
    includeIfNull: false,
  )


  final bool personalUseOnly;



      /// Sources included only because personalUse=true
  @JsonKey(
    
    name: r'personalUseSources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? personalUseSources;



  @JsonKey(
    
    name: r'excluded',
    required: false,
    includeIfNull: false,
  )


  final List<ExportLicenceSummaryExcludedInner>? excluded;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExportLicenceSummary &&
      other.licences == licences &&
      other.attributions == attributions &&
      other.personalUseOnly == personalUseOnly &&
      other.personalUseSources == personalUseSources &&
      other.excluded == excluded;

    @override
    int get hashCode =>
        licences.hashCode +
        attributions.hashCode +
        personalUseOnly.hashCode +
        personalUseSources.hashCode +
        excluded.hashCode;

  factory ExportLicenceSummary.fromJson(Map<String, dynamic> json) => _$ExportLicenceSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$ExportLicenceSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

