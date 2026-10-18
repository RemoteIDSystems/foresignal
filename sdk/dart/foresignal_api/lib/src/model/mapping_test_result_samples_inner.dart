//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/normalized_record_preview.dart';
import 'package:foresignal_api/src/model/mapping_warning.dart';
import 'package:foresignal_api/src/model/mapping_rejection.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_test_result_samples_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingTestResultSamplesInner {
  /// Returns a new [MappingTestResultSamplesInner] instance.
  MappingTestResultSamplesInner({

    required  this.records,

    required  this.rejections,

     this.skipped,

    required  this.warnings,

    required  this.receivedAt,
  });

  @JsonKey(
    
    name: r'records',
    required: true,
    includeIfNull: false,
  )


  final List<NormalizedRecordPreview> records;



  @JsonKey(
    
    name: r'rejections',
    required: true,
    includeIfNull: false,
  )


  final List<MappingRejection> rejections;



      /// Items dropped by `filter`
  @JsonKey(
    
    name: r'skipped',
    required: false,
    includeIfNull: false,
  )


  final int? skipped;



  @JsonKey(
    
    name: r'warnings',
    required: true,
    includeIfNull: false,
  )


  final List<MappingWarning> warnings;



  @JsonKey(
    
    name: r'receivedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime receivedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingTestResultSamplesInner &&
      other.records == records &&
      other.rejections == rejections &&
      other.skipped == skipped &&
      other.warnings == warnings &&
      other.receivedAt == receivedAt;

    @override
    int get hashCode =>
        records.hashCode +
        rejections.hashCode +
        skipped.hashCode +
        warnings.hashCode +
        receivedAt.hashCode;

  factory MappingTestResultSamplesInner.fromJson(Map<String, dynamic> json) => _$MappingTestResultSamplesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$MappingTestResultSamplesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

