//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/normalized_record_preview.dart';
import 'package:foresignal_api/src/model/mapping_warning.dart';
import 'package:foresignal_api/src/model/mapping_rejection.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_preview_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingPreviewResult {
  /// Returns a new [MappingPreviewResult] instance.
  MappingPreviewResult({

    required  this.records,

    required  this.rejections,

     this.skipped,

    required  this.warnings,
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





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingPreviewResult &&
      other.records == records &&
      other.rejections == rejections &&
      other.skipped == skipped &&
      other.warnings == warnings;

    @override
    int get hashCode =>
        records.hashCode +
        rejections.hashCode +
        skipped.hashCode +
        warnings.hashCode;

  factory MappingPreviewResult.fromJson(Map<String, dynamic> json) => _$MappingPreviewResultFromJson(json);

  Map<String, dynamic> toJson() => _$MappingPreviewResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

