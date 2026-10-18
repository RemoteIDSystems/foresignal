//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/licence_terms.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'export_licence_summary_licences_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExportLicenceSummaryLicencesInner {
  /// Returns a new [ExportLicenceSummaryLicencesInner] instance.
  ExportLicenceSummaryLicencesInner({

    required  this.terms,

    required  this.sources,

    required  this.rows,
  });

  @JsonKey(
    
    name: r'terms',
    required: true,
    includeIfNull: false,
  )


  final LicenceTerms terms;



  @JsonKey(
    
    name: r'sources',
    required: true,
    includeIfNull: false,
  )


  final List<String> sources;



  @JsonKey(
    
    name: r'rows',
    required: true,
    includeIfNull: false,
  )


  final int rows;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExportLicenceSummaryLicencesInner &&
      other.terms == terms &&
      other.sources == sources &&
      other.rows == rows;

    @override
    int get hashCode =>
        terms.hashCode +
        sources.hashCode +
        rows.hashCode;

  factory ExportLicenceSummaryLicencesInner.fromJson(Map<String, dynamic> json) => _$ExportLicenceSummaryLicencesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ExportLicenceSummaryLicencesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

