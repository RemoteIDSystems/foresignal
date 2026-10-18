//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'export_licence_summary_excluded_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExportLicenceSummaryExcludedInner {
  /// Returns a new [ExportLicenceSummaryExcludedInner] instance.
  ExportLicenceSummaryExcludedInner({

    required  this.sourceId,

    required  this.reason,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final ExportLicenceSummaryExcludedInnerReasonEnum reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExportLicenceSummaryExcludedInner &&
      other.sourceId == sourceId &&
      other.reason == reason;

    @override
    int get hashCode =>
        sourceId.hashCode +
        reason.hashCode;

  factory ExportLicenceSummaryExcludedInner.fromJson(Map<String, dynamic> json) => _$ExportLicenceSummaryExcludedInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ExportLicenceSummaryExcludedInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExportLicenceSummaryExcludedInnerReasonEnum {
@JsonValue(r'licence-forbidden')
licenceForbidden(r'licence-forbidden'),
@JsonValue(r'licence-local-only')
licenceLocalOnly(r'licence-local-only'),
@JsonValue(r'private')
private(r'private');

const ExportLicenceSummaryExcludedInnerReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


