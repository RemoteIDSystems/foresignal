//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/record_mapping.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_test_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingTestRequest {
  /// Returns a new [MappingTestRequest] instance.
  MappingTestRequest({

     this.mapping,
  });

  @JsonKey(
    
    name: r'mapping',
    required: false,
    includeIfNull: false,
  )


  final RecordMapping? mapping;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingTestRequest &&
      other.mapping == mapping;

    @override
    int get hashCode =>
        (mapping == null ? 0 : mapping.hashCode);

  factory MappingTestRequest.fromJson(Map<String, dynamic> json) => _$MappingTestRequestFromJson(json);

  Map<String, dynamic> toJson() => _$MappingTestRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

