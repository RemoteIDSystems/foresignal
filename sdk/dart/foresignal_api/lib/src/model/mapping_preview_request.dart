//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/record_mapping.dart';
import 'package:foresignal_api/src/model/mapping_sample.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_preview_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingPreviewRequest {
  /// Returns a new [MappingPreviewRequest] instance.
  MappingPreviewRequest({

    required  this.mapping,

    required  this.sample,

     this.sourceId,
  });

  @JsonKey(
    
    name: r'mapping',
    required: true,
    includeIfNull: false,
  )


  final RecordMapping mapping;



  @JsonKey(
    
    name: r'sample',
    required: true,
    includeIfNull: false,
  )


  final MappingSample sample;



      /// Custom source whose kinds, fieldSpecs, policy and observedAtMode are checked
  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingPreviewRequest &&
      other.mapping == mapping &&
      other.sample == sample &&
      other.sourceId == sourceId;

    @override
    int get hashCode =>
        mapping.hashCode +
        sample.hashCode +
        (sourceId == null ? 0 : sourceId.hashCode);

  factory MappingPreviewRequest.fromJson(Map<String, dynamic> json) => _$MappingPreviewRequestFromJson(json);

  Map<String, dynamic> toJson() => _$MappingPreviewRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

