//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_item_rejection_schema_errors_inner.dart';
import 'package:foresignal_api/src/model/edp_item_rejection.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_validation_report.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpValidationReport {
  /// Returns a new [EdpValidationReport] instance.
  EdpValidationReport({

     this.manifestValid,

     this.manifestErrors,

     this.breakingChanges,

     this.itemsValid,

     this.rejected,
  });

  @JsonKey(
    
    name: r'manifestValid',
    required: false,
    includeIfNull: false,
  )


  final bool? manifestValid;



  @JsonKey(
    
    name: r'manifestErrors',
    required: false,
    includeIfNull: false,
  )


  final List<EdpItemRejectionSchemaErrorsInner>? manifestErrors;



  @JsonKey(
    
    name: r'breakingChanges',
    required: false,
    includeIfNull: false,
  )


  final List<String>? breakingChanges;



  @JsonKey(
    
    name: r'itemsValid',
    required: false,
    includeIfNull: false,
  )


  final int? itemsValid;



  @JsonKey(
    
    name: r'rejected',
    required: false,
    includeIfNull: false,
  )


  final List<EdpItemRejection>? rejected;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpValidationReport &&
      other.manifestValid == manifestValid &&
      other.manifestErrors == manifestErrors &&
      other.breakingChanges == breakingChanges &&
      other.itemsValid == itemsValid &&
      other.rejected == rejected;

    @override
    int get hashCode =>
        (manifestValid == null ? 0 : manifestValid.hashCode) +
        manifestErrors.hashCode +
        breakingChanges.hashCode +
        itemsValid.hashCode +
        rejected.hashCode;

  factory EdpValidationReport.fromJson(Map<String, dynamic> json) => _$EdpValidationReportFromJson(json);

  Map<String, dynamic> toJson() => _$EdpValidationReportToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

